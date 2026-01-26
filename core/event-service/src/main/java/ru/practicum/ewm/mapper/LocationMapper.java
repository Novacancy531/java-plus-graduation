package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.config.CommonMapperConfiguration;
import ru.practicum.dto.event.EventLocationDto;
import ru.practicum.ewm.dal.entity.Location;

@Mapper(config = CommonMapperConfiguration.class)
public interface LocationMapper {

    Location toEntity(EventLocationDto dto);

    EventLocationDto toDto(Location entity);
}
