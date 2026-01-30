package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.EndpointHitDto;
import ru.practicum.core.config.CommonMapperConfiguration;
import ru.practicum.model.EndpointHit;

@Mapper(config = CommonMapperConfiguration.class)
public interface EndpointHitMapper {

    EndpointHitDto toDto(EndpointHit entity);

    @Mapping(target = "created", source = "dto.timestamp")
    EndpointHit toEntity(EndpointHitDto dto);
}
