package ru.practicum.events.service;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import ru.practicum.events.dto.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
public interface EventsService {
    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto addEvent(NewEventDto newEventDto, Long userId);

    EventFullDto getFullEvent(Long userId, Long eventId);

    List<EventFullDto> searchEvents(String text, List<Integer> categoriesIds, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from, Integer size);

    EventFullDto getEventById(Long id);

    List<EventFullDto> getEventsForAdmin(List<Integer> users, List<String> states, List<Integer> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest update);

    EventFullDto updateEventUser(Long userId, Long eventId, UpdateEventUserRequest update);
}
