package ru.practicum.events.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.ErrorHandler.EntityNotFoundException;
import ru.practicum.ErrorHandler.EventChangeException;
import ru.practicum.ErrorHandler.EventPublishException;
import ru.practicum.StatsClient;
import ru.practicum.StatsResponse;
import ru.practicum.categories.entity.CategoryEntity;
import ru.practicum.categories.repository.CategoryRepository;
import ru.practicum.compilations.entity.CompilationEntity;
import ru.practicum.events.dto.*;
import ru.practicum.events.entity.EventEntity;
import ru.practicum.events.entity.LocationEntity;
import ru.practicum.events.repository.EventsRepository;
import ru.practicum.events.repository.LocationRepository;
import ru.practicum.events.specifications.EventSpecifications;
import ru.practicum.users.repository.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class EventsServiceImpl implements EventsService {
    private final EventsRepository eventsRepository;
    private final EventEntityMapper mapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final StatsClient statsClient;


    @Override
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());

        return  eventsRepository.findAllByInitiatorId(userId, pageable)
                .stream()
                .map(mapper::toShortDto)
                .toList();
    }

    @Override
    public EventFullDto addEvent(NewEventDto newEventDto, Long userId) {
        //дату проверить лучше в дто
        EventEntity newEntity = mapper.toEntity(newEventDto);
        newEntity.setInitiator(userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(userId, "User")));
        newEntity.setCategory(categoryRepository.findById(newEventDto.getCategory()).orElseThrow(
                () -> new EntityNotFoundException(newEventDto.getCategory(), "Category")));
        newEntity.setState(EventState.PENDING);
        updateLocation(newEntity, mapper.toLocationEntity(newEventDto.getLocation()));
        if (newEventDto.getRequestModeration() == null) {
            newEntity.setRequestModeration(true);
            newEntity.setParticipantLimit(0);
        }
        return mapper.toFullDto(eventsRepository.save(newEntity));
    }

    @Override
    public EventFullDto getFullEvent(Long userId, Long eventId) {
        //404 нет такого эвента e": "Event with id=13 was not found",
        EventEntity entity = eventsRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(eventId, "Event"));
        if (!entity.getInitiator().getId().equals(userId)) {
            throw new EntityNotFoundException(eventId, "Event");
        }
        EventFullDto eventFull = mapper.toFullDto(entity);
        eventFull.setViews(getViewsForEvent(eventId, eventFull.getCreatedOn()));
        return eventFull;
    }

    @Override
    public List<EventFullDto> searchEvents(String text, List<Integer> categoriesIds, Boolean paid,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                           String sort, Integer from, Integer size) {

     //   Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());


        List<Long> categoryIdsLong = categoriesIds == null ? null :
                categoriesIds.stream()
                        .map(Integer::longValue)
                        .toList();


        Specification<EventEntity> spec = EventSpecifications.publicFilter(
                text,
                categoryIdsLong,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable

        );
        SortType sortType = SortType.from(sort);
        if (sortType == SortType.EVENT_DATE) {

            Pageable pageable = PageRequest.of(from / size,
                    size,
                    Sort.by("eventDate").ascending());

            return eventsRepository.findAll(spec, pageable)
                    .stream()
                    .map(mapper::toFullDto)
                    .peek(e -> e.setViews(getViewsForEvent(e.getId(), e.getCreatedOn())))
                    .toList();
        }

        // если нужно по вьюс
        List<EventFullDto> all = eventsRepository.findAll(spec)
                .stream()
                .map(mapper::toFullDto)
                .toList();

        // Получаем views
        all.forEach(e -> e.setViews(getViewsForEvent(e.getId(), e.getCreatedOn())));

        // Сортируем по views
        all = all.stream()
                .sorted(Comparator.comparingLong(EventFullDto::getViews))
                .toList();

        int start = Math.min(from, all.size());
        int end = Math.min(start + size, all.size());

        return all.subList(start, end);
    }

    @Override
    public EventFullDto getEventById(Long id) {
        // 404 если нет такого эвента
        EventEntity entity = eventsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id, "Event"));
        // должно быть уже опубликовано
        if (!entity.getState().equals(EventState.PUBLISHED) ) {
            throw new EntityNotFoundException(id, "Event");
        }
        EventFullDto eventFull = mapper.toFullDto(entity);
        eventFull.setViews(getViewsForEvent(id, eventFull.getCreatedOn()));
        return eventFull;

    }

    @Override
    public List<EventFullDto> getEventsForAdmin(List<Integer> users, List<String> states, List<Integer> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());

        List<Long> userIdsLong = users == null ? null :
                users.stream()
                        .map(Integer::longValue)
                        .toList();

        List<Long> categoryIdsLong = categories == null ? null :
                categories.stream()
                        .map(Integer::longValue)
                        .toList();
        List<EventState> stateEnums = states == null ? null :
                states.stream()
                        .map(EventState::valueOf)
                        .toList();

        Specification<EventEntity> spec = EventSpecifications.adminFilter(
                userIdsLong,
                stateEnums,
                categoryIdsLong,
                rangeStart,
                rangeEnd
        );

        return eventsRepository
                .findAll(spec, pageable)
                .stream()
                .map(mapper::toFullDto)
                .peek(event -> event.setViews(getViewsForEvent(event.getId(), event.getCreatedOn())))
                .toList();
    }

    @Override
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest update) {
        // 404 - событие не найдено
        EventEntity entity = eventsRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(eventId, "Event"));
        //409	- не удовлетворяет правилам редактирования

        //дата начала изменяемого события должна быть не ранее чем за час от даты публикации. (Ожидается код ошибки 409)
       //TODO

        //событие можно публиковать, только если оно в состоянии ожидания публикации (Ожидается код ошибки 409)
        //событие можно отклонить, только если оно еще не опубликовано (Ожидается код ошибки 409)
        if (update.getStateAction() != null)
            switch (update.getStateAction()) {
                case StateAction.PUBLISH_EVENT -> {
                    if (!entity.getState().equals(EventState.PENDING)) {
                        throw new EventPublishException(entity.getState().name());
                    }
                    entity.setState(EventState.PUBLISHED);
                    entity.setPublishedOn(LocalDateTime.now());
                }
                case StateAction.REJECT_EVENT -> {
                    if (entity.getState().equals(EventState.PUBLISHED)) {
                        throw new EventPublishException(entity.getState().name());
                    }
                    entity.setState(EventState.CANCELED);
                }
            }
        updateEntity(entity, mapper.toEntity(update));
        /*

        if (!(update.getAnnotation() == null)) {
            entity.setAnnotation(update.getAnnotation());
        }
        if (!(update.getDescription() == null)) {
            entity.setDescription(update.getDescription());
        }
        if (!(update.getEventDate() == null)) {
            entity.setEvent_date(update.getEventDate());
        }
        if (!(update.getLocation() == null)) {
            updateLocation(entity, update.getLocation());
        }
        if (!(update.getPaid() == null)) {
            entity.setPaid(update.getPaid());
        }
        if (!(update.getRequestModeration() == null)) {
            entity.setRequest_moderation(update.getRequestModeration());
        }
        if (!(update.getTitle() == null)) {
            entity.setTitle(update.getTitle());
        }

        if (!(update.getParticipantLimit()== null )) {
                if ( entity.getConfirmed_requests() <=update.getParticipantLimit()) {
                    entity.setParticipant_limit(update.getParticipantLimit());
                }
            } */
        EventFullDto eventFull = mapper.toFullDto(eventsRepository.save(entity));
        eventFull.setViews(getViewsForEvent(eventFull.getId(), eventFull.getCreatedOn()));
        return eventFull;

    }



    @Override
    public EventFullDto updateEventUser(Long userId, Long eventId, UpdateEventUserRequest update) {

        // 404 Событие не найдено или недоступно
        EventEntity entity = eventsRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(eventId, "Event"));
        //      409  Событие не удовлетворяет правилам редактирования -   "message": "Only pending or canceled events can be changed",
        if (entity.getState().equals(EventState.PUBLISHED)) {
            throw new EventChangeException();
        }

        // дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента (Ожидается код ошибки 409)
        //TODO

        if (StateAction.CANCEL_REVIEW.equals(update.getStateAction())) {
            entity.setState(EventState.CANCELED);
        }
        if (StateAction.SEND_TO_REVIEW.equals(update.getStateAction())) {
            entity.setState(EventState.PENDING);
        }
        updateEntity(entity,mapper.toEntity(update));
        EventFullDto eventFull = mapper.toFullDto(eventsRepository.save(entity));
        eventFull.setViews(getViewsForEvent(eventFull.getId(), eventFull.getCreatedOn()));
        return eventFull;
    }



    private void updateLocation(EventEntity entity, LocationEntity location) {
        Optional<LocationEntity> locationEntity = locationRepository.findByLatAndLon(location.getLat(), location.getLon());
                   if (locationEntity.isEmpty()) {
                        LocationEntity newLocation = new LocationEntity();
                       newLocation.setLat(location.getLat());
                        newLocation.setLon(location.getLon());
                        locationEntity = Optional.of(locationRepository.save(newLocation));
                        }
                   entity.setLocation(locationEntity.get());
        }

         private void updateEntity(EventEntity entity, EventEntity update) {
             if (!(update.getAnnotation() == null)) {
                 entity.setAnnotation(update.getAnnotation());
             }
             if (!(update.getDescription() == null)) {
                 entity.setDescription(update.getDescription());
             }
             if (!(update.getEventDate() == null)) {
                 entity.setEventDate(update.getEventDate());
             }
             if (!(update.getLocation() == null)) {
                 updateLocation(entity, update.getLocation());
             }
             if (!(update.getPaid() == null)) {
                 entity.setPaid(update.getPaid());
             }
             if (!(update.getCategory() == null)) {
                 entity.setCategory(categoryRepository.findById(update.getCategory().getId()).orElseThrow(
                         () -> new EntityNotFoundException(update.getCategory().getId(), "Category")));
             }
             if (!(update.getRequestModeration() == null)) {
                 entity.setRequestModeration(update.getRequestModeration());
             }
             if (!(update.getTitle() == null)) {
                 entity.setTitle(update.getTitle());
             }

             if (!(update.getParticipantLimit()== null )) {
                 if ( entity.getConfirmedRequests() <=update.getParticipantLimit()) {
                     entity.setParticipantLimit(update.getParticipantLimit());
                 }
             }

         }

    private long getViewsForEvent(Long eventId, LocalDateTime createdOn) {
        List<String> uris = List.of("/events/" + eventId);

        List<StatsResponse> stats = statsClient.getStats(
                createdOn,
                LocalDateTime.now(),
                uris,
                true
        );

        if (stats == null || stats.isEmpty()) {
            return 0L;
        }

        return stats.getFirst().getHits();
    }


}
