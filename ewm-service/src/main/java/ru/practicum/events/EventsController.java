package ru.practicum.events;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.StatsClient;
import ru.practicum.events.dto.*;
import ru.practicum.events.service.EventsService;

import java.util.List;

@RestController
@RequestMapping(path = "/")
@RequiredArgsConstructor
@Slf4j
@Validated
public class EventsController {
    private final EventsService eventsService;
    private final StatsClient statsClient;


    //private

    //   GET /users/{userId}/events?from={from}&size={size})
    @GetMapping("users/{userId}/events")
    List<EventShortDto> getUserEvents(@PathVariable Long userId,
                                      @RequestParam(defaultValue = "0") Integer from,
                                      @RequestParam(defaultValue = "10") Integer size) {
        return eventsService.getUserEvents(userId, from, size);
    }

    // POST   /users/{userId}/events
    @PostMapping("users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    EventFullDto addEvent(@RequestBody @Valid NewEventDto newEventDto, @PathVariable Long userId) {
        return eventsService.addEvent(newEventDto, userId);
    }

    // GET   /users/{userId}/events/{eventId}
    @GetMapping("users/{userId}/events/{eventId}")
    EventFullDto getFullEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventsService.getFullEvent(userId, eventId);
    }

    // PATCH   /users/{userId}/events/{eventId}
    @PatchMapping("users/{userId}/events/{eventId}")
    EventFullDto updateUserEvent(@PathVariable Long userId, @PathVariable Long eventId,
                                 @RequestBody @Valid UpdateEventUserRequest update) {
        return eventsService.updateEventUser(userId, eventId, update);

    }

    //public

    //  GET  /events?text={text}&categories={}&paid={paid}&rangeStart={rangeStart}&rangeEnd={rangeEnd}&
    //  onlyAvailable={onlyAvailable}&sort={sort}&from={from}&size={size}
    @GetMapping("events")
    List<EventFullDto> getPublicEvents(
            HttpServletRequest request,
            @ModelAttribute @Valid SearchPublicFilterDto filterDto
    ) {
        statsClient.postHit(request);

        return eventsService.searchEvents(filterDto);
    }


    // GET   /events/{id}
    @GetMapping("events/{id}")
    EventFullDto getEventById(HttpServletRequest request, @PathVariable Long id) {
        statsClient.postHit(request);
        return eventsService.getEventById(id);
    }


    // admin

    // GET  admin/events?users=2&states=string&categories=2&rangeStart=222&rangeEnd=222&from=0&size=10
    @GetMapping("admin/events")
    List<EventFullDto> getAdminEvents(@ModelAttribute @Valid SearchAdminFilterDto filterDto) {

        return eventsService.getEventsForAdmin(filterDto);
    }


    //  PATCH  /admin/events/{eventId}
    @PatchMapping("admin/events/{eventId}")
    EventFullDto updateEventAdmin(@PathVariable Long eventId, @RequestBody @Valid UpdateEventAdminRequest update) {
        return eventsService.updateEventAdmin(eventId, update);
    }


}
