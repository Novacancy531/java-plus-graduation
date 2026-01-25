package ru.practicum.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import ru.practicum.dal.entity.User;
import ru.practicum.dal.repository.UserRepository;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;

import java.util.List;


@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final UserMapper mapper;


    @Transactional
    public UserDto create(UserDto userDto) {
        log.info("Создать пользователя. email: {}", userDto.getEmail());

        User user;
        try {
            user = repository.save(User.newUser(userDto.getName(), userDto.getEmail()));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Адрес электронной почты уже используется: " + ex.getMessage());
        }

        log.info("Создание пользователя OK, id = {}", user.getId());
        return mapper.toDto(user);
    }

    @Transactional
    public void delete(Long userId) {
        if (!userIsExist(userId)) {
            throw new NotFoundException("Удаляемая запись не найдена");
        }
        repository.deleteById(userId);
        log.info("Удален пользователь id = {}", userId);
    }

    @Transactional(readOnly = true)
    public List<UserDto> findUsers(List<Long> ids, Pageable pageable) {
        var result = (CollectionUtils.isEmpty(ids))
                ? repository.findAll(pageable).stream().toList()
                : repository.findAllById(ids);
        return result.stream()
                .map(mapper::toDto)
                .toList();

    }

    @Transactional(readOnly = true)
    public List<UserDto> findUsers(List<Long> ids, int from, int size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        return findUsers(ids, pageable);
    }

    @Transactional(readOnly = true)
    public boolean userIsExist(Long userId) {
        return repository.existsById(userId);
    }

    @Transactional(readOnly = true)
    public UserDto findUser(Long userId) {
        return mapper.toDto(repository.findById(userId).orElseThrow(()
                -> new NotFoundException("Пользователь с id=" + userId + " не найден")));

    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        return mapper.toDto(repository.findById(userId).orElseThrow(()->
                new NotFoundException("Пользователь с id=" + userId + " не найден")));
    }
}
