package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.EventControllerApi;

@FeignClient(name = "event-service")
public interface EventControllerEvent extends EventControllerApi {
}
