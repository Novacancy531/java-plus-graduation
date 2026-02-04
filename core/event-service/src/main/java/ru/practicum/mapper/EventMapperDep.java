package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dal.entity.Event;

@UtilityClass
public class EventMapperDep {

    private final CategoryMapper categoryMapper = new CategoryMapperImpl();
    private final LocationMapper locationMapper = new LocationMapperImpl();

    public EventFullDto eventToFullDto(Event event, Long confirmedRequests, double rating) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(event.getInitiator())
                .location(locationMapper.toDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .rating(rating)
                .build();
    }

    public EventShortDto eventToShortDto(Event event, Long confirmedRequests, double rating) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .eventDate(event.getEventDate())
                .initiator(event.getInitiator())
                .paid(event.getPaid())
                .title(event.getTitle())
                .rating(rating)
                .build();
    }
}
