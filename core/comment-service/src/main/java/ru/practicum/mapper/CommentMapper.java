package ru.practicum.mapper;

import org.mapstruct.*;
import ru.practicum.config.CommonMapperConfiguration;
import ru.practicum.dal.entity.Comment;
import ru.practicum.dto.comment.CommentDto;

@Mapper(config = CommonMapperConfiguration.class)
public interface CommentMapper {

    @Mapping(target = "authorName", source = "authorId")
    @Mapping(target = "event", source = "eventId")
    CommentDto toDto(Comment entity);
}
