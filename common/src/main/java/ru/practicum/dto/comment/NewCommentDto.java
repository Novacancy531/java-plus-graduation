package ru.practicum.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCommentDto {

    @NotNull
    private Long eventId;

    @NotBlank(message = "Не заполнен комментарий")
    @Size(max = 2000, message = "Количество символов в комментарии не более 2000.")
    private String text;
}
