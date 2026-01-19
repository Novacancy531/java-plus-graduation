package ru.practicum.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.practicum.interfaceValidation.CreateValidation;
import ru.practicum.interfaceValidation.UpdateValidation;

@Data
@SuperBuilder
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationCommonDto {

    private Long id;

    @NotBlank(message = "Заголовок обязателен к заполнению", groups = CreateValidation.class)
    @Size(max = 50, message = "Количество символов заголовка должно быть в диапазоне от 1 до 50",
            groups = {CreateValidation.class, UpdateValidation.class})
    private String title;

    private Boolean pinned = false;

}
