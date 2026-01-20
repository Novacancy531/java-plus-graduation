package ru.practicum.ewm.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.core.config.CommonMapperConfiguration;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.model.Request;

@Mapper(config = CommonMapperConfiguration.class)
public interface RequestMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "requester", source = "requesterId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "created", source = "created")
    @Mapping(target = "id", source = "id")
    ParticipationRequestDto toDto(Request entity);
}
