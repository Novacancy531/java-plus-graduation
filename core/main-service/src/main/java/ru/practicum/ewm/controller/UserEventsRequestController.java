package ru.practicum.ewm.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.service.RequestService;
import ru.practicum.exception.ConditionsException;
import ru.practicum.exception.ConflictException;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
@Slf4j
public class UserEventsRequestController {

    private final RequestService service;

    @GetMapping
    public List<ParticipationRequestDto> findForEvent(
            @Positive @PathVariable Long userId,
            @Positive @PathVariable Long eventId) throws ConditionsException {
        return service.getRequestsForEventOwner(userId, eventId);
    }

    @PatchMapping
    public EventRequestStatusUpdateResult update(
            @Positive @PathVariable Long userId,
            @Positive @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest dto) throws ConditionsException, ConflictException {
        return service.updateRequestStatus(userId, eventId, dto);
    }
}
