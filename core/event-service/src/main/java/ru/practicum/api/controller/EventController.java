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
    public List<EventShortDto> find(EventsFilter filter, int from, int size, String clientIp, String requestUri) {
        return service.findPublicEventsWithFilter(filter, from, size, clientIp, requestUri);
    }

    @Override
    public EventFullDto findById(Long id, String clientIp, String requestUri) {
        return service.findPublicEventById(id, clientIp, requestUri);
    }

    @Override
    public EventFullDto getEventById(Long id) {
        return service.getEvent(id);
    }
}
