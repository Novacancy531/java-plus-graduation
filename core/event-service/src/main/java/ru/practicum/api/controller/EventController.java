package ru.practicum.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.api.EventControllerApi;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.domain.service.EventService;
import ru.practicum.filter.EventsFilter;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
public class EventController implements EventControllerApi {

    private final EventService service;

    @Override
    public List<EventShortDto> find(EventsFilter filter, int from, int size) {
        return service.findPublicEventsWithFilter(filter, from, size);
    }

    @Override
    public EventFullDto getEvent(Long id, Long userId) {
        return service.findPublicEventById(id, userId);
    }

    @Override
    public EventFullDto getEventById(Long id) {
        return service.getEvent(id);
    }

    @Override
    public List<EventShortDto> recommendations(Long userId, int maxResults) {
        return service.getRecommendations(userId, maxResults);
    }
}
