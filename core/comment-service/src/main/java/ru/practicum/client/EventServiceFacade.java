package ru.practicum.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.event.EventFullDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceFacade {

    private final EventControllerClient eventClient;

    @CircuitBreaker(name = "eventService", fallbackMethod = "getEventFallback")
    @Retry(name = "eventService")
    public EventFullDto getEventById(Long eventId) {
        return eventClient.getEventById(eventId);
    }

    private EventFullDto getEventFallback(Long eventId, Throwable ex) {
        log.warn("event-service недоступен, return null eventId={}, reason={}", eventId, ex.toString());
        return null;
    }
}
