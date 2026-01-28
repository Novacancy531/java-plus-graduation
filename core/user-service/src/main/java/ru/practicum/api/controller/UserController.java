package ru.practicum.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.UserControllerApi;
import ru.practicum.domain.service.UserService;
import ru.practicum.dto.user.UserDto;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
public class UserController implements UserControllerApi {

    private final UserService service;

    @Override
    public List<UserDto> find(List<Long> ids, int from, int size) {
        return service.findUsers(ids, from, size);
    }

    @Override
    public UserDto create(@Valid UserDto dto) {
        return service.create(dto);
    }

    @Override
    public void delete(long userId) {
        service.delete(userId);
    }

    @Override
    public boolean existsById(Long id) {
        return service.userIsExist(id);
    }

    @Override
    public UserDto getUserById(long id) {
        return service.getUserById(id);
    }
}
