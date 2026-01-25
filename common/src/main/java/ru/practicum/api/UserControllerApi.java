package ru.practicum.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.UserDto;

import java.util.List;


public interface UserControllerApi {

    String PATH = "/admin/users";

    @GetMapping(PATH)
    List<UserDto> find(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );

    @PostMapping(PATH)
    @ResponseStatus(HttpStatus.CREATED)
    UserDto create(@RequestBody @Valid UserDto dto);

    @DeleteMapping(PATH + "/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@Positive @PathVariable long userId);

    @GetMapping(PATH + "/{id}/exists")
    boolean existsById(@PathVariable Long id);

    @GetMapping(PATH + "/{id}")
    UserDto getUserById(@PathVariable long id);
}
