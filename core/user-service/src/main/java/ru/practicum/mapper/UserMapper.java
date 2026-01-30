package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.config.CommonMapperConfiguration;
import ru.practicum.dal.entity.User;
import ru.practicum.dto.user.UserDto;

@Mapper(config = CommonMapperConfiguration.class)
public interface UserMapper {

    UserDto toDto(User entity);

    @Mapping(target = "email", ignore = true)
    UserDto toUserDtoShort(User entity);
}
