package ru.practicum.api;

import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.compilation.CompilationFullDto;

import java.util.List;

public interface CompilationControllerApi {

    String PATH = "/compilations";

    @GetMapping(PATH)
    List<CompilationFullDto> find(
            @RequestParam(required = false) Boolean pinned, @RequestParam(defaultValue = "0") int from,
            @Positive @RequestParam(defaultValue = "10") int size);

    @GetMapping(PATH + "/{compId}")
    CompilationFullDto findById(@Positive @PathVariable Long compId);
}
