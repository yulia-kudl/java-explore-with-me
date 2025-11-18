package ru.practicum.requests.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ErrorHandler.EntityNotFoundException;
import ru.practicum.ErrorHandler.RequestException;
import ru.practicum.events.dto.EventState;
import ru.practicum.events.entity.EventEntity;
import ru.practicum.events.repository.EventsRepository;
import ru.practicum.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.dto.RequestStatus;
import ru.practicum.requests.entity.RequestEntity;
import ru.practicum.requests.repository.RequestsRepository;
import ru.practicum.users.entity.UserEntity;
import ru.practicum.users.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestsRepository requestsRepository;
    private final RequestEntityMapper mapper;
    private final UserRepository userRepository;
    private final EventsRepository eventsRepository;

    @Override
    public List<ParticipationRequestDto> getParticipationRequests(Long userId) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(userId, "User"));

        return requestsRepository.findAllByUserId(userId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        EventEntity eventEntity = eventsRepository.findById(eventId).orElseThrow(
                () -> new EntityNotFoundException(eventId, "Event"));

        UserEntity userEntity = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(userId, "User"));

        if (requestsRepository.findByUser_IdAndEvent_Id(userId, eventId).isPresent()) {
            throw new RequestException("нельзя добавить повторный запрос");
        }
        if (Objects.equals(eventEntity.getInitiator().getId(), userId)) {
            throw new RequestException("Инициатор события не может добавить запрос на участие в своём событии ");

        }
        if (!eventEntity.getState().equals(EventState.PUBLISHED)) {
            throw new RequestException("нельзя участвовать в неопубликованном событии");

        }
        if (eventEntity.getParticipantLimit() == eventEntity.getConfirmedRequests().intValue() &&
                eventEntity.getParticipantLimit() != 0) {
            throw new RequestException("у события достигнут лимит запросов на участие");
        }

        RequestEntity requestEntity = new RequestEntity();
        requestEntity.setEvent(eventEntity);
        requestEntity.setUser(userEntity);
        //если для события отключена пре-модерация запросов на участие, то запрос должен автоматически перейти в состояние подтвержденного

        if (!eventEntity.getRequestModeration() || eventEntity.getParticipantLimit() == 0) {
            requestEntity.setStatus(RequestStatus.CONFIRMED);
            eventEntity.setConfirmedRequests(eventEntity.getConfirmedRequests() + 1);

        } else {
            requestEntity.setStatus(RequestStatus.PENDING);
        }
        eventsRepository.save(eventEntity);
        return mapper.toDto(requestsRepository.save(requestEntity));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        // 404 request not found
        RequestEntity requestEntity = requestsRepository.findById(requestId).orElseThrow(
                () -> new EntityNotFoundException(requestId, "Request"));
        Long eventId = requestEntity.getEvent().getId();
        EventEntity eventEntity = eventsRepository.findById(eventId).orElseThrow(
                () -> new EntityNotFoundException(eventId, "Event"));

        // Если заявка была подтверждена, уменьшаем confirmed_requests
        if (requestEntity.getStatus() == RequestStatus.CONFIRMED) {
            Long confirmed = eventEntity.getConfirmedRequests();
            eventEntity.setConfirmedRequests(confirmed - 1);

        }

        // Устанавливаем статус отменённого запроса
        requestEntity.setStatus(RequestStatus.CANCELED);

        // Сохраняем изменения
        requestsRepository.save(requestEntity);
        eventsRepository.save(eventEntity);

        return mapper.toDto(requestEntity);

    }

    @Override
    public List<ParticipationRequestDto> getRequestForUser(Long userId, Long eventId) {
        List<RequestEntity> requestEntity = requestsRepository.findByEvent_Id(eventId);

        return requestEntity
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(EventRequestStatusUpdateRequest request,
                                                              Long userId,
                                                              Long eventId) {

        EventEntity eventEntity = eventsRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(eventId, "Event"));

        if (eventEntity.getParticipantLimit() != 0 &&
                eventEntity.getParticipantLimit() == eventEntity.getConfirmedRequests().intValue()) {
            throw new RequestException("нет свободных мест у эвента");
        }

        List<RequestEntity> requests = requestsRepository.findByIdIn(request.getRequestIds());
        if (requests.isEmpty()) {
            throw new RequestException("Список пуст");
        }

        List<ParticipationRequestDto> confirmedDtos = new ArrayList<>();
        List<ParticipationRequestDto> rejectedDtos = new ArrayList<>();
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult(confirmedDtos, rejectedDtos);

        // Проверяем что заявки в PENDING
        boolean allPending = requests.stream()
                .allMatch(r -> r.getStatus().equals(RequestStatus.PENDING));
        if (!allPending) {
            throw new RequestException("Статус можно изменить только у заявок в состоянии ожидания");
        }


        int confirmed = eventEntity.getConfirmedRequests().intValue();
        int limit = eventEntity.getParticipantLimit();


// если нужно отклонить
        if (request.getStatus().equals(RequestStatus.REJECTED)) {

            for (RequestEntity r : requests) {
                r.setStatus(RequestStatus.REJECTED);
                requestsRepository.save(r);
                rejectedDtos.add(mapper.toDto(r));
            }

            return result;
        }

// подтверждение
        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {

            for (RequestEntity r : requests) {
                if (confirmed >= limit) { // нужно отменить свыше лимита
                    r.setStatus(RequestStatus.REJECTED);
                    requestsRepository.save(r);
                    rejectedDtos.add(mapper.toDto(r));
                    continue;
                }

                // Подтверждаем
                r.setStatus(RequestStatus.CONFIRMED);
                requestsRepository.save(r);
                confirmedDtos.add(mapper.toDto(r));
                confirmed++;
            }

            // Обновляем число подтверждённых
            eventEntity.setConfirmedRequests((long) confirmed);


            if (confirmed >= limit) {
                eventEntity.setAvailable(false);
            }

            eventsRepository.save(eventEntity);

            return result;
        }

        return result;
    }

}
