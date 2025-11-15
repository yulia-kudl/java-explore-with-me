package ru.practicum.events;

import jakarta.validation.Valid;
import jdk.dynalink.linker.LinkerServices;
import jdk.jfr.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CurrentTimestamp;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.dto.*;
import ru.practicum.events.service.EventsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/")
@RequiredArgsConstructor
@Slf4j
@Validated
public class EventsController {
    private final EventsService eventsService;


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


// TODO
  //  GET  /users/{userId}/events/{eventId}/requests

    //TODO
  //  PATCH    /users/{userId}/events/{eventId}/requests


    //public

  //  GET  /events?text={text}&categories={}&paid={paid}&rangeStart={rangeStart}&rangeEnd={rangeEnd}&
    //  onlyAvailable={onlyAvailable}&sort={sort}&from={from}&size={size}
    @GetMapping("events")
    List<EventFullDto> getPublicEvents(
            @RequestParam String text,
            @RequestParam List<Integer> categories,
            @RequestParam Boolean paid,
            @RequestParam LocalDateTime rangeStart,
            @RequestParam LocalDateTime rangeEnd,
            @RequestParam Boolean onlyAvailable,
            @RequestParam String sort,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
            ) {
        // TODO вызвать статистику
        rangeStart = rangeStart == null ? LocalDateTime.now() : rangeStart;

        return eventsService.searchEvents(text, categories,paid, rangeStart,
                rangeEnd, onlyAvailable, sort, from, size);
    }


    // GET   /events/{id}
    @GetMapping("events/{id}")
    EventFullDto getEventById(@PathVariable Long id) {
        // TODO добавить статистику
        return eventsService.getEventById(id);
    }


    // admin

   // GET  admin/events?users=2&states=string&categories=2&rangeStart=222&rangeEnd=222&from=0&size=10
    @GetMapping("admin/events")
    List<EventFullDto> getAdminEvents(
            @RequestParam List<Integer> users,
            @RequestParam List<String> states,
            @RequestParam List<Integer> categories,
            @RequestParam LocalDateTime rangeStart,
            @RequestParam LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {

        return eventsService.getEventsForAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }


  //  PATCH  /admin/events/{eventId}
    @PatchMapping("admin/events/{eventId}")
    EventFullDto updateEventAdmin(@PathVariable Long eventId, @RequestBody UpdateEventAdminRequest update) {
        return  eventsService.updateEventAdmin(eventId, update);
    }


}
