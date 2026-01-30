package ru.practicum.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.api.UserControllerApi;
import ru.practicum.dto.user.UserDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceFacade {

    private final UserControllerApi userClient;

    @CircuitBreaker(name = "userService", fallbackMethod = "existsFallback")
    @Retry(name = "userService")
    public boolean existsById(Long userId) {
        return userClient.existsById(userId);
    }

    private boolean existsFallback(Long userId, Throwable ex) {
        log.warn("user-service на данный момент не доступен, user id={}", userId, ex);
        return false;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getFallback")
    @Retry(name = "userService")
    public UserDto getUserById(Long userId) {
        return userClient.getUserById(userId);
    }

    private UserDto getFallback(Long userId, Throwable ex) {
        log.warn("user-service unavailable, return null id={}", userId, ex);
        return null;
    }
}
