package ru.practicum.dto.comment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentStatusDto {
    private Boolean deleted;
}
