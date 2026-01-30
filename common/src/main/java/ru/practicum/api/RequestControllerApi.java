package ru.practicum.api;

import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.constant.RequestStatus;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;


public interface RequestControllerApi {

    @GetMapping("/users/{userId}/requests")
    List<ParticipationRequestDto> findByUser(@Positive @PathVariable Long userId);

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    ParticipationRequestDto create(@Positive @PathVariable Long userId, @RequestParam Long eventId);

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    ParticipationRequestDto cancel(@Positive @PathVariable Long userId, @Positive @PathVariable Long requestId);

    @GetMapping("/requests/count")
    Long countByEventIdAndStatus(@RequestParam long eventId, @RequestParam RequestStatus requestStatus);

    @PatchMapping("/requests/events/{eventId}")
    EventRequestStatusUpdateResult updateRequestStatus(@PathVariable Long eventId, @RequestParam Long ownerId,
                                                       @RequestBody EventRequestStatusUpdateRequest updateDto
    );

    @GetMapping("/requests/events/{eventId}")
    List<ParticipationRequestDto> getRequestsForEventOwner(@PathVariable Long eventId, @RequestParam Long ownerId);

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    List<ParticipationRequestDto> findForEvent(@Positive @PathVariable Long userId,
                                               @Positive @PathVariable Long eventId);

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    EventRequestStatusUpdateResult update(@Positive @PathVariable Long userId, @Positive @PathVariable Long eventId,
                                          @RequestBody EventRequestStatusUpdateRequest dto);
}

