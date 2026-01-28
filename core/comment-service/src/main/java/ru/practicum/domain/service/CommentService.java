package ru.practicum.domain.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.client.EventServiceFacade;
import ru.practicum.client.UserServiceFacade;
import ru.practicum.dal.entity.Comment;
import ru.practicum.dal.repository.CommentRepository;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserServiceFacade userServiceClient;
    private final EventServiceFacade eventServiceClient;
    private final CommentMapper commentMapper;

    @Transactional(readOnly = true)
    public CommentDto findById(long id) {
        log.info("Получение комментария с id: {}", id);
        return commentMapper.toDto(getCommentOrThrow(id));
    }

    @Transactional
    public CommentDto create(NewCommentDto dto, Long userId) {
        var user = getUserOrThrow(userId);
        var event = getEventOrThrow(dto.getEventId());
        var comment = Comment.create(user.getId(), event.getId(), dto.getText());

        log.info("Создание нового комментария к событию с id={} пользователем с id={}", dto.getEventId(), userId);
        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Transactional
    public CommentDto update(UpdateCommentDto dto, Long userId, Long id) {
        var comment = getCommentOrThrow(id);
        comment.update(userId, dto.getText());

        log.info("Обновление комментария к событию с id={} пользователем с id {}", comment.getEventId(), userId);
        return commentMapper.toDto(comment);
    }

    @Transactional
    public void deleteById(Long id, Long userId) {
        var comment = getCommentOrThrow(id);
        comment.delete(userId);

        log.info("Комментарий с id={} удалён пользователем с id={}", id, userId);
    }

    @Transactional
    public void deleteCommentAsAdmin(Long id) {
        var comment = getCommentOrThrow(id);

        comment.deleteByAdmin();
        log.info("Комментарий с id={} удалён администратором.", id);
    }


    @Transactional(readOnly = true)
    public List<CommentDto> findAllCommentsForEvent(Long eventId) {
        var event = getEventOrThrow(eventId);

        log.info("Получаем комментарии по событию с id={}", eventId);
        var comments = commentRepository.findCommentsByEvent(eventId).orElse(new ArrayList<>());

        log.info("Возвращаем {} комментариев события с id={}", comments.size(), eventId);
        return comments.stream()
                .map(commentMapper::toDto)
                .toList();
    }

    private Comment getCommentOrThrow(Long commentId) {
        return commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id=" + commentId + " не найден."));
    }

    private UserDto getUserOrThrow(Long userId) {
        UserDto user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден.");
        }
        return user;
    }

    private EventFullDto getEventOrThrow(Long eventId) {
        EventFullDto event = eventServiceClient.getEventById(eventId);
        if (event == null) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено.");
        }
        return event;
    }
}
