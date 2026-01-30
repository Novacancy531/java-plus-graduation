package ru.practicum.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.api.CategoryControllerApi;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.domain.service.CategoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class CategoryController implements CategoryControllerApi {
    private final CategoryService service;

    @Override
    public List<CategoryDto> find(int from, int size) {
        return service.find(from, size);
    }

    @Override
    public CategoryDto findById(Long catId) {
        return service.getEntity(catId);
    }
}
