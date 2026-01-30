package ru.practicum.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.config.CommonMapperConfiguration;
import ru.practicum.dal.entity.Request;
import ru.practicum.dto.request.ParticipationRequestDto;

@Mapper(config = CommonMapperConfiguration.class)
public interface RequestMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "event", source = "eventId")
    @Mapping(target = "requester", source = "requesterId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "created", source = "created")
    @Mapping(target = "id", source = "id")
    ParticipationRequestDto toDto(Request entity);
}
