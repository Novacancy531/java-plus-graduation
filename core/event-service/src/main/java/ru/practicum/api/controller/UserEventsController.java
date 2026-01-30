package ru.practicum.api.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventNewDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.EventUpdateDto;
import ru.practicum.domain.service.EventService;
import ru.practicum.exception.ConditionsException;
import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
public class UserEventsController {
    private final EventService service;

    @GetMapping
    public List<EventShortDto> find(
            @Positive @PathVariable Long userId,
            @PageableDefault(page = 0, size = 10, sort = "createdOn", direction = Sort.Direction.DESC) Pageable pageable
    ) throws ConditionsException {
        return service.findByUserId(userId, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(
            @Positive @PathVariable Long userId,
            @RequestBody @Valid EventNewDto dto) {
        return service.create(dto, userId);
    }

    @GetMapping("/{eventId}")
    public EventFullDto findByUserIdAndEventId(
            @Positive @PathVariable Long userId,
            @Positive @PathVariable Long eventId) {
        return service.findByUserIdAndEventId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(
            @Positive @PathVariable Long userId,
            @Positive @PathVariable Long eventId,
            @RequestBody @Valid EventUpdateDto dto) {
        return service.update(userId, eventId, dto);
    }


}
