package ru.practicum.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.api.CommentControllerApi;
import ru.practicum.domain.service.CommentService;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.UpdateCommentDto;
import ru.practicum.dto.comment.NewCommentDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController implements CommentControllerApi {
    private final CommentService commentService;

    @Override
    public CommentDto findById(Long id) {
        return commentService.findById(id);
    }

    @Override
    public CommentDto create(@Valid NewCommentDto entity, Long userId) {
        return commentService.create(entity, userId);
    }

    @Override
    public CommentDto update(@Valid UpdateCommentDto entity, Long userId,
                             Long id) {
        return commentService.update(entity, userId, id);
    }

    @Override
    public void delete(Long id, Long userId) {
        commentService.deleteById(id, userId);
    }

    @Override
    public List<CommentDto> findAllCommentsForEvent(Long eventId) {
        return commentService.findAllCommentsForEvent(eventId);
    }
}
