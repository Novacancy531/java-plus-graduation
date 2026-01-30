package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.UserControllerApi;

@FeignClient(name = "user-service")
public interface UserControllerClient extends UserControllerApi {
}
