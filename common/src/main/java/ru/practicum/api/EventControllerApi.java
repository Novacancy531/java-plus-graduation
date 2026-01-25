package ru.practicum.api;

import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.filter.EventsFilter;

import java.util.List;

public interface EventControllerApi {

    String PATH = "/events";

    @GetMapping(PATH)
    List<EventShortDto> find(
            EventsFilter filter,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "X-Client-Ip", required = false) String clientIp,
            @RequestHeader(value = "X-Request-Uri", required = false) String requestUri
    );

    @GetMapping(PATH + "/{id}")
    EventFullDto findById(
            @PathVariable @Positive Long id,
            @RequestHeader(value = "X-Client-Ip", required = false) String clientIp,
            @RequestHeader(value = "X-Request-Uri", required = false) String requestUri
    );

    @GetMapping(PATH + "/internal/{id}")
    EventFullDto getEventById(@PathVariable @Positive Long id);
}
