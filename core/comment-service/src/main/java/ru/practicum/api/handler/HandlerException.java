package ru.practicum.api.handler;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.ConditionsException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class HandlerException {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ru.practicum.error.ErrorMessage> notFoundException(NotFoundException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ru.practicum.error.ErrorMessage(exception.getMessage()));
    }

    @ExceptionHandler(ConditionsException.class)
    public ResponseEntity<ru.practicum.error.ErrorMessage> conditionsException(ConditionsException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ru.practicum.error.ErrorMessage(exception.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ru.practicum.error.ErrorMessage> conflictException(ConflictException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ru.practicum.error.ErrorMessage(exception.getMessage()));
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ru.practicum.error.ErrorMessage> handleConstraintViolation(ConstraintViolationException exception) {
        String errorMessage = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        log.error("ConstraintViolation: {}", errorMessage);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ru.practicum.error.ErrorMessage(errorMessage));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ru.practicum.error.ErrorMessage> handleConstraintViolation(IllegalArgumentException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ru.practicum.error.ErrorMessage(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ru.practicum.error.ErrorMessage> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {

        String errorMessage = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> "%s: %s".formatted(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));
        log.error(errorMessage);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ru.practicum.error.ErrorMessage(errorMessage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ru.practicum.error.ErrorMessage> handleAllExceptions(Exception exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ru.practicum.error.ErrorMessage("Произошла непредвиденная ошибка"));
    }


    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ru.practicum.error.ErrorMessage> handleMissingParams(MissingServletRequestParameterException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ru.practicum.error.ErrorMessage("Произошла непредвиденная ошибка"));
    }
}
