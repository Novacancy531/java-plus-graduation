package ru.practicum.api;

import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;

import java.util.List;


public interface CategoryControllerApi {

    String PATH = "/categories";

    @GetMapping(PATH)
    List<CategoryDto> find(@RequestParam(defaultValue = "0") int from,
                           @Positive @RequestParam(defaultValue = "10") int size);

    @GetMapping(PATH + "/{catId}")
    CategoryDto findById(@Positive @PathVariable Long catId);
}
