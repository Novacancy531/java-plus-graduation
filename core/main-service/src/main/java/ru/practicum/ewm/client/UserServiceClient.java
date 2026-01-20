package ru.practicum.ewm.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.UserServiceApi;

@FeignClient(name = "user-service")
public interface UserServiceClient extends UserServiceApi {
}
