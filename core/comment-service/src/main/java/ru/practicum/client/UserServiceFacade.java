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

    private final UserControllerClient userService;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    @Retry(name = "userService")
    public UserDto getUserById(Long userId) {
        return userService.getUserById(userId);
    }

    private UserDto getUserFallback(Long userId, Throwable ex) {
        log.warn("user-service недоступен, return null userId={}, reason={}", userId, ex.toString());
        return null;
    }
}
