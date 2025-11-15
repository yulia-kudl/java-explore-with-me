package ru.practicum.requests.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.catalina.User;
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

import java.sql.Timestamp;
import java.time.LocalDateTime;
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
                .map(mapper:: toDto)
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
        if (!eventEntity.getAvailable()) {
            throw new RequestException("у события достигнут лимит запросов на участие");
        }

        RequestEntity requestEntity = new RequestEntity();
        requestEntity.setEvent(eventEntity);
        requestEntity.setUser(userEntity);
        //если для события отключена пре-модерация запросов на участие, то запрос должен автоматически перейти в состояние подтвержденного

        if (!eventEntity.getRequest_moderation()) {
            requestEntity.setStatus(RequestStatus.CONFIRMED);
            eventEntity.setConfirmed_requests(eventEntity.getConfirmed_requests() + 1);
            if (eventEntity.getParticipant_limit() == eventEntity.getConfirmed_requests().intValue()) {
                eventEntity.setAvailable(Boolean.FALSE);
            }

        } else {
            requestEntity.setStatus(RequestStatus.PENDING);
        }
        eventsRepository.save(eventEntity);
        return mapper.toDto(requestsRepository.save(requestEntity));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long eventId) {
        // 404 request not found
        RequestEntity requestEntity = requestsRepository.findByUser_IdAndEvent_Id(userId, eventId).orElseThrow(
                () -> new EntityNotFoundException(userId, "Request"));
        EventEntity eventEntity = eventsRepository.findById(eventId).orElseThrow(
                () -> new EntityNotFoundException(eventId, "Event"));

        // Если заявка была подтверждена, уменьшаем confirmed_requests
        if (requestEntity.getStatus() == RequestStatus.CONFIRMED) {
            Long confirmed = eventEntity.getConfirmed_requests();
            eventEntity.setConfirmed_requests(confirmed - 1);

            // Событие снова доступно, если был лимит
            if (eventEntity.getParticipant_limit() > 0 &&
                    eventEntity.getConfirmed_requests() < eventEntity.getParticipant_limit()) {
                eventEntity.setAvailable(true);
            }
        }

        // Устанавливаем статус отменённого запроса
        requestEntity.setStatus(RequestStatus.REJECTED);

        // Сохраняем изменения
        requestsRepository.save(requestEntity);
        eventsRepository.save(eventEntity);

        return mapper.toDto(requestEntity);

    }

    @Override
    public ParticipationRequestDto getRequestForUser(Long userId, Long eventId) {
        RequestEntity requestEntity = requestsRepository.findByUser_IdAndEvent_Id(userId, eventId).orElseThrow(
                () -> new EntityNotFoundException(userId, "Request"));
        EventEntity eventEntity = eventsRepository.findById(eventId).orElseThrow(
                () -> new EntityNotFoundException(eventId, "Event"));


        return mapper.toDto(requestEntity);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(EventRequestStatusUpdateRequest request, Long userId, Long eventId) {

        EventEntity eventEntity = eventsRepository.findById(eventId).orElseThrow(
                () -> new EntityNotFoundException(eventId, "Event"));


        List<RequestEntity> requests = requestsRepository.findByUser_IdAndEvent_IdIn(userId, request.getIds());
        if (requests.isEmpty()) {
            throw new RequestException("список пуст");
        }

        List<ParticipationRequestDto> confirmedDtos = new ArrayList<>();
        List<ParticipationRequestDto> rejectedDtos = new ArrayList<>();
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult(confirmedDtos, rejectedDtos);

        if ( eventEntity.getConfirmed_requests().intValue() == eventEntity.getParticipant_limit() &&
                request.getStatus().equals(RequestStatus.CONFIRMED)) {
            throw new RequestException("нельзя подтвердить заявку, если уже достигнут лимит по заявкам" +
                    " на данное событие ");
        }
        //если при подтверждении данной заявки, лимит заявок для события исчерпан, то все неподтверждённые заявки необходимо отклонить

       long count =  requests.stream()
                .filter(requestEntity -> requestEntity.getStatus().equals(RequestStatus.PENDING))
                .count();
        if (count != requests.size()) {
            throw  new RequestException("статус можно изменить только у заявок, находящихся в состоянии ожидания");

        }

        // нужно отменить все

        if (request.getStatus().equals(RequestStatus.REJECTED)) {
            requests.forEach(requestEntity -> {
                requestEntity.setStatus(RequestStatus.REJECTED);
                requestsRepository.save(requestEntity);
            });

            //return re

        }

        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
            //empty
        }








        //если при подтверждении данной заявки, лимит заявок для события исчерпан, то все неподтверждённые заявки необходимо отклонить
        return null;
    }
}
