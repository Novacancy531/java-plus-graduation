package ru.practicum.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.domain.service.CommentService;

@RestController
@RequestMapping(path = "/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {
    private final CommentService commentService;

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        commentService.deleteCommentAsAdmin(id);
    }
}
