package ru.practicum.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.user.UserDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceFacade {

    private final UserControllerClient userClient;

    @CircuitBreaker(name = "userService", fallbackMethod = "existsFallback")
    @Retry(name = "userService")
    public boolean existsById(Long userId) {
        return userClient.existsById(userId);
    }

    private boolean existsFallback(Long userId, Throwable ex) {
        log.warn(
                "user-service недоступен, existsById=false для userId={}, reason={}",
                userId,
                ex.getMessage()
        );
        return false;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    @Retry(name = "userService")
    public UserDto getUserById(long userId) {
        return userClient.getUserById(userId);
    }

    private UserDto getUserFallback(long userId, Throwable ex) {
        log.warn(
                "user-service недоступен, return null user для userId={}, reason={}",
                userId,
                ex.getMessage()
        );
        return null;
    }
}
