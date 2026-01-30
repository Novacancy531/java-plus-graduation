package ru.practicum.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.api.EventControllerApi;
import ru.practicum.dto.event.EventFullDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceFacade {

    private final EventControllerApi eventClient;

    @CircuitBreaker(name = "eventService", fallbackMethod = "getFallback")
    @Retry(name = "eventService")
    public EventFullDto getEventById(Long eventId) {
        return eventClient.getEventById(eventId);
    }

    private EventFullDto getFallback(Long eventId, Throwable ex) {
        log.warn("event-service на данный момент не доступен, eventId={}", eventId, ex);
        return null;
    }
}
