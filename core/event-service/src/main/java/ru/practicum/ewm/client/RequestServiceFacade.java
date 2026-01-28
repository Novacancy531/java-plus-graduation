package ru.practicum.ewm.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.constant.RequestStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceFacade {

    private final RequestControllerClient client;

    @CircuitBreaker(name = "requestService", fallbackMethod = "countFallback")
    @Retry(name = "requestService")
    public Long countConfirmed(long eventId) {
        return client.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    private Long countFallback(long eventId, Throwable ex) {
        log.warn(
                "request-service недоступен, confirmedRequests=0 for eventId={}, reason={}",
                eventId,
                ex.getMessage()
        );
        return 0L;
    }
}
