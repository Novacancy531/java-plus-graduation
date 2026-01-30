package ru.practicum.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentDto;

import java.util.List;

public interface CommentControllerApi {

    String PATH = "/comments";

    @GetMapping(PATH + "/{id}")
    @ResponseStatus(HttpStatus.OK)
    CommentDto findById(@PathVariable Long id);

    @PostMapping(PATH)
    @ResponseStatus(HttpStatus.CREATED)
    CommentDto create(@RequestBody @Valid NewCommentDto entity, @RequestHeader("X-User-Id") Long userId);

    @PatchMapping(PATH + "/{id}")
    @ResponseStatus(HttpStatus.OK)
    CommentDto update(@RequestBody @Valid UpdateCommentDto entity, @RequestHeader("X-User-Id") Long userId,
                      @PathVariable Long id);

    @DeleteMapping(PATH + "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId);

    @GetMapping(PATH + "/event/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    List<CommentDto> findAllCommentsForEvent(@PathVariable Long eventId);
}
