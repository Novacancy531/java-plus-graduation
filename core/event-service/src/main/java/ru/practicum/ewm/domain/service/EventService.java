package ru.practicum.ewm.domain.service;

import com.querydsl.core.BooleanBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.constant.EventState;
import ru.practicum.constant.EventStateAction;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventNewDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.EventUpdateDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.ewm.client.RequestServiceFacade;
import ru.practicum.ewm.client.StatsServiceFacade;
import ru.practicum.ewm.client.UserServiceFacade;
import ru.practicum.exception.ConditionsException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.filter.EventsFilter;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.mapper.EventMapperDep;
import ru.practicum.ewm.dal.entity.Category;
import ru.practicum.ewm.dal.entity.Event;
import ru.practicum.ewm.dal.repository.CategoryRepository;
import ru.practicum.ewm.dal.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class EventService {

    private final EventRepository repository;
    private final EventMapper mapper;


    private final UserServiceFacade userServiceClient;
    private final RequestServiceFacade requestServiceClient;
    private final StatsServiceFacade statsServiceClient;
    private final LocationService locationService;
    private final CategoryRepository categoryRepository;

    @Transactional
    public EventFullDto create(@Valid EventNewDto dto, Long userId) throws ConditionsException {
        var userDto = getUserOrThrow(userId);
        var category = getCategoryOrThrow(dto.getCategory());
        var location = locationService.getOrCreateLocation(dto.getLocation());

        Event event = mapper.toEntityWithNewDto(dto, userDto.getId(), category, location);
        event = event.toBuilder().createdOn(LocalDateTime.now()).build();
        event = repository.save(event);
        log.info("Создано событие с id = {}", event.getId());

        return mapper.toDto(event);
    }

    @Transactional
    public EventFullDto update(Long userId, Long eventId, @Valid EventUpdateDto dto) {
        if (!userIsExist(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        Event event = getEventOrThrow(eventId, userId);
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя изменять опубликованное событие");
        }

        var category = (dto.getCategory() == null) ? null : getCategoryOrThrow(dto.getCategory());
        var location = (dto.getLocation() == null) ? null : locationService.getOrCreateLocation(dto.getLocation());

        EventState state = dto.getStateAction() == null ?
                null :
                switch (dto.getStateAction()) {
                    case SEND_TO_REVIEW -> EventState.PENDING;
                    case CANCEL_REVIEW -> EventState.CANCELED;
                    case PUBLISH_EVENT, REJECT_EVENT -> null;
                };
        event = mapper.toEntityWithUpdateDto(event, dto, category, location, state);
        event = repository.save(event);
        log.info("Обновлено событие с id = {}", eventId);

        Long calcConfirmedRequests = getConfirmedRequests(eventId);
        Long calcView = statsServiceClient.getViewsForEvent(eventId);

        return mapper.toDto(event).toBuilder()
                .confirmedRequests(calcConfirmedRequests)
                .views(calcView)
                .build();
    }

    @Transactional
    public EventFullDto updateAdmin(Long eventId, @Valid EventUpdateDto dto) {
        Event event = getEventOrThrow(eventId);

        EventState currentState = event.getState();
        EventStateAction action = dto.getStateAction();
        LocalDateTime newDate = dto.getEventDate();

        if (action != null) {
            if (action == EventStateAction.PUBLISH_EVENT) {
                if (currentState != EventState.PENDING) {
                    throw new ConflictException("Можно публиковать только события в состоянии PENDING");
                }
            } else if (action == EventStateAction.REJECT_EVENT) {
                if (currentState == EventState.PUBLISHED) {
                    throw new ConflictException("Нельзя отклонить опубликованное событие");
                }
            }
        }
        if (dto.getEventDate() != null) {
            validateEventDate(newDate, action, currentState, event);
        }

        EventState state = action == null ?
                null :
                switch (action) {
                    case PUBLISH_EVENT -> EventState.PUBLISHED;
                    case REJECT_EVENT -> EventState.CANCELED;
                    case SEND_TO_REVIEW, CANCEL_REVIEW -> null;
                };

        LocalDateTime eventDate = dto.getEventDate() == null ? null : newDate;
        var category = (dto.getCategory() == null) ? null : getCategoryOrThrow(dto.getCategory());
        var location = (dto.getLocation() == null) ? null : locationService.getOrCreateLocation(dto.getLocation());
        dto = dto.toBuilder().eventDate(eventDate).build();

        event = mapper.toEntityWithUpdateDto(event, dto, category, location, state);
        event = repository.save(event);
        log.info("Администратор обновил событие с id = {}", eventId);

        Long calcConfirmedRequests = getConfirmedRequests(eventId);
        Long calcView = statsServiceClient.getViewsForEvent(eventId);
        return mapper.toDto(event).toBuilder()
                .confirmedRequests(calcConfirmedRequests)
                .views(calcView)
                .build();

    }

    @Transactional(readOnly = true)
    public EventFullDto findByUserIdAndEventId(Long userId, Long eventId) {
        if (!userIsExist(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        Event event = getEventOrThrow(eventId, userId);

        Long calcConfirmedRequests = getConfirmedRequests(eventId);
        Long calcView = statsServiceClient.getViewsForEvent(eventId);
        log.info("Получено событие {} пользователя {}", eventId, userId);
        return mapper.toDto(event).toBuilder()
                .confirmedRequests(calcConfirmedRequests)
                .views(calcView)
                .build();

    }

    @Transactional(readOnly = true)
    public EventFullDto findPublicEventById(Long eventId, String clientIp, String requestUri) {
        Event event = getEventOrThrow(eventId, EventState.PUBLISHED);
        Long calcConfirmedRequests = getConfirmedRequests(eventId);
        Long calcView = statsServiceClient.getViewsForEvent(eventId);

        statsServiceClient.saveHit(
                "main-service",
                requestUri,
                clientIp,
                LocalDateTime.now()
        );

        log.info("Получено публичное событие {}", eventId);
        return mapper.toDto(event).toBuilder()
                .confirmedRequests(calcConfirmedRequests)
                .views(calcView)
                .build();
    }

    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long eventId) {
        Event event = getEventOrThrow(eventId);
        Long calcConfirmedRequests = getConfirmedRequests(eventId);
        Long calcView = statsServiceClient.getViewsForEvent(eventId);

        log.info("Получено событие {}", eventId);
        return mapper.toDto(event).toBuilder()
                .confirmedRequests(calcConfirmedRequests)
                .views(calcView)
                .build();
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> findByUserId(Long userId, Pageable pageable) {
        if (!userIsExist(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        return repository.findAllByinitiator(userId, pageable)
                .stream()
                .map(event -> EventMapperDep.eventToShortDto(
                        event,
                        getConfirmedRequests(event.getId()),
                        statsServiceClient.getViewsForEvent(event.getId())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> findPublicEventsWithFilter(@Valid EventsFilter filter, int from, int size,
                                                          String clientIp, String requestUri) {
        statsServiceClient.saveHit(
                "main-service",
                requestUri,
                clientIp,
                LocalDateTime.now()
        );

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        return findEventsWithFilterInternal(
                filter,
                pageable,
                false,
                (event, viewsMap) -> {
                    String uri = "/events/" + event.getId();
                    Long views = viewsMap.getOrDefault(uri, 0L);
                    return EventMapperDep.eventToShortDto(event, getConfirmedRequests(event.getId()), views);
                }
        );
    }

    @Transactional(readOnly = true)
    public List<EventFullDto> findAdminEventsWithFilter(@Valid EventsFilter filter, Pageable pageable) {
        return findEventsWithFilterInternal(
                filter,
                pageable,
                true,
                (event, viewsMap) -> {
                    String uri = "/events/" + event.getId();
                    Long views = viewsMap.getOrDefault(uri, 0L);
                    return EventMapperDep.eventToFullDto(event, getConfirmedRequests(event.getId()), views);
                }
        );
    }

    private <T> List<T> findEventsWithFilterInternal(
            EventsFilter filter,
            Pageable pageable,
            Boolean forAdmin,
            BiFunction<Event, Map<String, Long>, T> mapper) {

        BooleanBuilder predicate = EventPredicateBuilder.buildPredicate(filter, forAdmin);

        if (!forAdmin) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "eventDate")

            );
        }
        Page<Event> eventsPage = repository.findAll(predicate, pageable);

        if (eventsPage.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> uris = eventsPage.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        Map<String, Long> viewsUriMap = statsServiceClient.getViewsForUris(uris);
        Stream<Event> eventStream = eventsPage.stream();
        List<T> result = eventStream
                .map(e -> mapper.apply(e, viewsUriMap))
                .toList();

        log.info("Найдено {} событий в режиме {}", result.size(), forAdmin ? "ADMIN" : "PUBLIC");
        return result;
    }

    private UserDto getUserOrThrow(Long userId) {
        UserDto user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return user;
    }

    private Category getCategoryOrThrow(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new ConditionsException("Категория с id=" + catId + " не найдена"));
    }

    private Event getEventOrThrow(Long eventId) {
        return repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
    }

    private Event getEventOrThrow(Long eventId, Long userId) {
        var event = getEventOrThrow(eventId);
        if (!event.getInitiator().equals(userId)) {
            throw new ConditionsException("Пользователь не является инициатором события");
        }
        return event;
    }

    private Event getEventOrThrow(Long eventId, EventState state) {
        var event = getEventOrThrow(eventId);
        if (event.getState() != state) {
            throw new NotFoundException("Событие не в состоянии " + state);
        }
        return event;
    }

    private Long getConfirmedRequests(Long eventId) {
        return requestServiceClient.countConfirmed(eventId);
    }

    @Transactional(readOnly = true)
    public boolean userIsExist(Long userId) {
        return userServiceClient.existsById(userId);
    }

    private void validateEventDate(LocalDateTime newDate, EventStateAction action, EventState currentState,
                                   Event event)  {
        if (action == EventStateAction.PUBLISH_EVENT) {
            if (newDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConditionsException("Дата начала должна быть не ранее чем через 1 час при публикации");
            }
        } else if (currentState == EventState.PUBLISHED && event.getPublishedOn() != null) {
            if (newDate.isBefore(event.getPublishedOn().plusHours(1))) {
                throw new ConditionsException("Дата начала должна быть не ранее чем через 1 час после публикации");
            }
        }
    }
}
