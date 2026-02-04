package ru.practicum.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.domain.service.EventViewService;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
public class EventViewController {
    private final EventViewService eventViewService;

    @PutMapping("/{eventId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void like(
            @RequestHeader("X-EWM-USER-ID") Long userId,
            @PathVariable Long eventId
    ) {
        eventViewService.likeEvent(userId, eventId);
    }
}
