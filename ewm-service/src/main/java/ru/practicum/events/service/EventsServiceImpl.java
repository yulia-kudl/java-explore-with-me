package ru.practicum.events.service;

import lombok.AllArgsConstructor;
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
import ru.practicum.categories.repository.CategoryRepository;
import ru.practicum.events.SortType;
import ru.practicum.events.dto.*;
import ru.practicum.events.entity.EventEntity;
import ru.practicum.events.entity.LocationEntity;
import ru.practicum.events.repository.EventsRepository;
import ru.practicum.events.repository.LocationRepository;
import ru.practicum.events.specifications.EventSpecifications;
import ru.practicum.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

        return eventsRepository.findAllByInitiatorId(userId, getPageable(from, size, SortType.ID))
                .stream()
                .map(mapper::toShortDto)
                .toList();
    }

    @Override
    public EventFullDto addEvent(NewEventDto newEventDto, Long userId) {
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
        //404 нет такого эвента
        EventEntity entity = eventsRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(eventId, "Event"));
        if (!entity.getInitiator().getId().equals(userId)) {
            throw new EntityNotFoundException(eventId, "Event");
        }
        EventFullDto eventFull = mapper.toFullDto(entity);
        eventFull.setViews(getViewsForEvent(eventId, eventFull.getPublishedOn()));
        return eventFull;
    }

    @Override
    public List<EventFullDto> searchEvents(SearchPublicFilterDto filterDto) {

        List<Long> categoryIdsLong = filterDto.getCategories() == null ? null :
                filterDto.getCategories().stream()
                        .map(Integer::longValue)
                        .toList();


        Specification<EventEntity> spec = EventSpecifications.publicFilter(
                filterDto.getText(),
                categoryIdsLong,
                filterDto.getPaid(),
                filterDto.getRangeStart(),
                filterDto.getRangeEnd(),
                filterDto.getOnlyAvailable()

        );

        if (filterDto.getSort() != null && filterDto.getSort() == SortType.EVENT_DATE) {

            List<EventFullDto> results = eventsRepository.findAll(
                            spec,
                            getPageable(filterDto.getFrom(), filterDto.getSize(), filterDto.getSort())
                    ).stream()
                    .map(mapper::toFullDto)
                    .toList();
            return addViewsToList(results);
        }

        // если нужно сортировать по вьюс
        List<EventFullDto> all = eventsRepository.findAll(spec)
                .stream()
                .map(mapper::toFullDto)
                .toList();

        // Добавляем views
        all = addViewsToList(all);

// 3. Сортируем по views
        all = all.stream()
                .sorted(Comparator.comparingLong(EventFullDto::getViews))
                .toList();

        int start = Math.min(filterDto.getFrom(), all.size());
        int end = Math.min(start + filterDto.getSize(), all.size());

        return all.subList(start, end);
    }

    @Override
    public EventFullDto getEventById(Long id) {
        // 404 если нет такого эвента
        EventEntity entity = eventsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id, "Event"));
        // должно быть уже опубликовано
        if (!entity.getState().equals(EventState.PUBLISHED)) {
            throw new EntityNotFoundException(id, "Event");
        }
        EventFullDto eventFull = mapper.toFullDto(entity);
        eventFull.setViews(getViewsForEvent(id, eventFull.getPublishedOn()));
        return eventFull;

    }

    @Override
    public List<EventFullDto> getEventsForAdmin(SearchAdminFilterDto filterDto) {

        List<Long> userIdsLong = filterDto.getUsers() == null ? null :
                filterDto.getUsers().stream()
                        .map(Integer::longValue)
                        .toList();

        List<Long> categoryIdsLong = filterDto.getCategories() == null ? null :
                filterDto.getCategories().stream()
                        .map(Integer::longValue)
                        .toList();
        List<EventState> stateEnums = filterDto.getStates();

        Specification<EventEntity> spec = EventSpecifications.adminFilter(
                userIdsLong,
                stateEnums,
                categoryIdsLong,
                filterDto.getRangeStart(),
                filterDto.getRangeEnd()
        );

        List<EventFullDto> events = eventsRepository
                .findAll(spec, getPageable(filterDto.getFrom(), filterDto.getSize(), SortType.ID))
                .stream()
                .map(mapper::toFullDto)
                .toList();

// Массовое добавление просмотров
        events = addViewsToList(events);

        return events;
    }

    @Override
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest update) {
        // 404 - событие не найдено
        EventEntity entity = eventsRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(eventId, "Event"));

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

        EventFullDto eventFull = mapper.toFullDto(eventsRepository.save(entity));
        eventFull.setViews(getViewsForEvent(eventFull.getId(), eventFull.getPublishedOn()));
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


        if (StateAction.CANCEL_REVIEW.equals(update.getStateAction())) {
            entity.setState(EventState.CANCELED);
        }
        if (StateAction.SEND_TO_REVIEW.equals(update.getStateAction())) {
            entity.setState(EventState.PENDING);
        }
        updateEntity(entity, mapper.toEntity(update));
        EventFullDto eventFull = mapper.toFullDto(eventsRepository.save(entity));
        eventFull.setViews(getViewsForEvent(eventFull.getId(), eventFull.getPublishedOn()));
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

        if (!(update.getParticipantLimit() == null)) {
            if (entity.getConfirmedRequests() <= update.getParticipantLimit()) {
                entity.setParticipantLimit(update.getParticipantLimit());
            }
        }

    }

    private List<EventFullDto> addViewsToList(List<EventFullDto> events) {

        LocalDateTime minDate = eventsRepository.findMinPublishedDate();

        // Если минимальной даты нет — значит нет опубликованных событий → все views = 0
        if (minDate == null) {
            return events.stream()
                    .peek(event -> event.setViews(0L))
                    .toList();
        }

        // Собираем URIs
        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        // запрос в stats
        List<StatsResponse> stats = statsClient.getStats(
                minDate,
                LocalDateTime.now(),
                uris,
                false
        );

        Map<Long, Integer> hitsMap = stats.stream()
                .collect(Collectors.toMap(
                        s -> extractIdFromUri(s.getUri()),
                        StatsResponse::getHits
                ));

        // Добавляем views в DTO
        return events.stream()
                .peek(event -> {
                    long views = hitsMap.getOrDefault(event.getId(), 0).longValue();
                    event.setViews(views);
                })
                .toList();
    }


    private Long extractIdFromUri(String uri) {
        //  /events/123
        return Long.parseLong(uri.substring(uri.lastIndexOf('/') + 1));
    }


    private long getViewsForEvent(Long eventId, LocalDateTime publishedOn) {
        if (publishedOn == null) {
            return 0;
        }
        List<String> uris = List.of("/events/" + eventId);

        List<StatsResponse> stats = statsClient.getStats(
                publishedOn,
                LocalDateTime.now(),
                uris,
                true
        );

        if (stats == null || stats.isEmpty()) {
            return 0L;
        }

        return stats.getFirst().getHits();
    }


    private Pageable getPageable(Integer from, Integer size, SortType sort) {
        switch (sort) {
            case EVENT_DATE -> {
                return PageRequest.of(from / size, size, Sort.by("eventDate").ascending());
            }
            case VIEWS -> {
                return PageRequest.of(from / size, size, Sort.by("views").ascending());

            }
            default -> {
                return PageRequest.of(from / size, size, Sort.by("id").ascending());
            }
        }


    }


}
