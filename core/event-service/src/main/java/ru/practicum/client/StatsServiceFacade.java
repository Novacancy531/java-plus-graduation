package ru.practicum.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.domain.service.StatsService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceFacade {

    private final StatsService statsClient;

    @CircuitBreaker(name = "statsService", fallbackMethod = "viewsForEventFallback")
    public Long getViewsForEvent(Long eventId) {
        return statsClient.getViewsForEvent(eventId);
    }

    private Long viewsForEventFallback(Long eventId, Throwable ex) {
        log.warn("stats-service недоступен, views=0 для eventId={}, reason={}", eventId, ex.toString());
        return 0L;
    }

    @CircuitBreaker(name = "statsService", fallbackMethod = "viewsForUrisFallback")
    public Map<String, Long> getViewsForUris(List<String> uris) {
        return statsClient.getViewsForUris(uris);
    }

    private Map<String, Long> viewsForUrisFallback(List<String> uris, Throwable ex) {
        log.warn("stats-service недоступен, viewsMap пустой, reason = {}", ex.toString());
        return Collections.emptyMap();
    }

    @CircuitBreaker(name = "statsService", fallbackMethod = "saveHitFallback")
    public void saveHit(String app, String uri, String ip, LocalDateTime ts) {
        statsClient.saveHit(app, uri, ip, ts);
    }

    private void saveHitFallback(String app, String uri, String ip, LocalDateTime ts, Throwable ex) {
        log.warn("stats-service недоступен, hit проигнорирован uri={}, reason={}", uri, ex.toString());
    }
}
