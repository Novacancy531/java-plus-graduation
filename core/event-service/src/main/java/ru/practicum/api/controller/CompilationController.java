package ru.practicum.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.CompilationControllerApi;
import ru.practicum.dto.compilation.CompilationFullDto;
import ru.practicum.domain.service.CompilationService;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
public class CompilationController implements CompilationControllerApi {
    private final CompilationService service;

    @Override
    public List<CompilationFullDto> find(Boolean pinned, int from, int size) {
        return service.find(pinned, from, size);
    }

    @Override
    public CompilationFullDto findById(Long compId) {
        return service.getEntityFool(compId);
    }
}
