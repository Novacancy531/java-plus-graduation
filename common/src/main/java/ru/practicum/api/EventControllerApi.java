package ru.practicum.api;

import jakarta.validation.Valid;
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
            @Valid EventsFilter filter,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );

    @GetMapping(PATH + "/{id}")
    EventFullDto getEvent(
            @PathVariable @Positive Long id,
            @RequestHeader("X-EWM-USER-ID") Long userId
    );

    @GetMapping(PATH + "/internal/{id}")
    EventFullDto getEventById(@PathVariable @Positive Long id);

    @GetMapping(PATH + "/recommendations")
     List<EventShortDto> recommendations(
            @RequestHeader("X-EWM-USER-ID") Long userId,
            @RequestParam(defaultValue = "10") int maxResults
    );
}
