package ru.practicum.ewm.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.RequestControllerApi;

@FeignClient(name = "request-service")
public interface RequestControllerClient extends RequestControllerApi {
}
