package ru.practicum.requests;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.service.RequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestsController {
    private final RequestService requestService;

    // GET  /users/{userId}/requests     Получение информации о заявках тек
    @GetMapping("/users/{userId}/requests")
    List<ParticipationRequestDto> getParticipationRequest(@PathVariable Long userId) {
        return requestService.getParticipationRequests(userId);
    }



    // POST /users/2/requests?eventId=3
    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    ParticipationRequestDto addRequest(@PathVariable Long userId, @RequestParam(required = true) Long eventId) {
        return  requestService.addRequest(userId, eventId);
    }

   //  PATCH    /users/{userId}/requests/{requestId}/cancel
    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    ParticipationRequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }


    //  GET  /users/{userId}/events/{eventId}/requests
    @GetMapping("/users/{userId}/events/{eventId}/requests")
    List<ParticipationRequestDto> getRequestForUser( @PathVariable Long userId, @PathVariable Long eventId) {
        return requestService.getRequestForUser(userId, eventId);
    }

    //  PATCH    /users/{userId}/events/{eventId}/requests
    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    EventRequestStatusUpdateResult  updateRequestStatus(@RequestBody @Valid EventRequestStatusUpdateRequest request,
                                                        @PathVariable Long userId,
                                                        @PathVariable Long eventId) {
        return requestService.updateRequestStatus(request, userId, eventId);
    }
}
