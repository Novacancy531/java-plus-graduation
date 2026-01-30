package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.config.CommonMapperConfiguration;
import ru.practicum.dto.event.EventLocationDto;
import ru.practicum.dal.entity.Location;

@Mapper(config = CommonMapperConfiguration.class)
public interface LocationMapper {

    Location toEntity(EventLocationDto dto);

    EventLocationDto toDto(Location entity);
}
