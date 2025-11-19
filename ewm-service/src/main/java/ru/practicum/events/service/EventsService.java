package ru.practicum.events.service;

import org.springframework.stereotype.Service;
import ru.practicum.events.dto.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
public interface EventsService {
    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto addEvent(NewEventDto newEventDto, Long userId);

    EventFullDto getFullEvent(Long userId, Long eventId);

    List<EventFullDto> searchEvents(SearchPublicFilterDto filterDto);

    EventFullDto getEventById(Long id);

    List<EventFullDto> getEventsForAdmin(SearchAdminFilterDto filterDto);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest update);

    EventFullDto updateEventUser(Long userId, Long eventId, UpdateEventUserRequest update);
}
