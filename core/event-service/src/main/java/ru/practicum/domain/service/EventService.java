package ru.practicum.domain.service;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.AnalyzerClient;
import ru.practicum.collector.CollectorClient;
import ru.practicum.constant.EventState;
import ru.practicum.constant.EventStateAction;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventNewDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.EventUpdateDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.client.RequestServiceFacade;
import ru.practicum.client.UserServiceFacade;
import ru.practicum.exception.ConditionsException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.filter.EventsFilter;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.EventMapperDep;
import ru.practicum.dal.entity.Category;
import ru.practicum.dal.entity.Event;
import ru.practicum.dal.repository.CategoryRepository;
import ru.practicum.dal.repository.EventRepository;
import ru.practicum.stats.proto.RecommendedEventProto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.constant.EventStateAction.REJECT_EVENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository repository;
    private final EventMapper mapper;

    private final UserServiceFacade userServiceClient;
    private final RequestServiceFacade requestServiceClient;
    private final LocationService locationService;
    private final CategoryRepository categoryRepository;

    private final CollectorClient collectorClient;
    private final AnalyzerClient analyzerClient;

    @Transactional
    public EventFullDto create(EventNewDto dto, Long userId) {
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
    public EventFullDto update(Long userId, Long eventId, EventUpdateDto dto) {
        if (!userIsExist(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        Event event = getEventOrThrow(eventId, userId);
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя изменять опубликованное событие");
        }

        var category = (dto.getCategory() == null) ? null : getCategoryOrThrow(dto.getCategory());
        var location = (dto.getLocation() == null) ? null : locationService.getOrCreateLocation(dto.getLocation());
        var state = mapActionToState(dto.getStateAction(), false);

        event = mapper.toEntityWithUpdateDto(event, dto, category, location, state);
        event = repository.save(event);
        log.info("Обновлено событие с id = {}", eventId);

        var confirmed = getConfirmedRequests(eventId);
        var rating = getRating(eventId);

        return mapToEventFullDto(event, confirmed, rating);
    }

    @Transactional
    public EventFullDto updateAdmin(Long eventId, EventUpdateDto dto) {
        var event = getEventOrThrow(eventId);

        EventState currentState = event.getState();
        EventStateAction action = dto.getStateAction();
        LocalDateTime newDate = dto.getEventDate();

        if (action != null) {
            if (action == EventStateAction.PUBLISH_EVENT) {
                if (currentState != EventState.PENDING) {
                    throw new ConflictException("Можно публиковать только события в состоянии PENDING");
                }
            } else if (action == REJECT_EVENT) {
                if (currentState == EventState.PUBLISHED) {
                    throw new ConflictException("Нельзя отклонить опубликованное событие");
                }
            }
        }
        if (dto.getEventDate() != null) {
            validateEventDate(newDate, action, currentState, event);
        }

        LocalDateTime eventDate = dto.getEventDate() == null ? null : newDate;
        var state = mapActionToState(action, true);
        var category = (dto.getCategory() == null) ? null : getCategoryOrThrow(dto.getCategory());
        var location = (dto.getLocation() == null) ? null : locationService.getOrCreateLocation(dto.getLocation());
        dto = dto.toBuilder().eventDate(eventDate).build();

        event = mapper.toEntityWithUpdateDto(event, dto, category, location, state);
        event = repository.save(event);
        log.info("Администратор обновил событие с id = {}", eventId);

        var confirmed = getConfirmedRequests(eventId);
        var rating = getRating(eventId);

        return mapToEventFullDto(event, confirmed, rating);
    }

    @Transactional(readOnly = true)
    public EventFullDto findByUserIdAndEventId(Long userId, Long eventId) {
        if (!userIsExist(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        var event = getEventOrThrow(eventId, userId);

        log.info("Получено событие {} пользователя {}", eventId, userId);

        var confirmed = getConfirmedRequests(eventId);
        var rating = getRating(eventId);

        return mapToEventFullDto(event, confirmed, rating);
    }

    @Transactional(readOnly = true)
    public EventFullDto findPublicEventById(Long eventId, Long userId) {
        var event = getEventOrThrow(eventId, EventState.PUBLISHED);
        var confirmed = getConfirmedRequests(eventId);

        collectorClient.view(userId, eventId);

        var rating = getRating(eventId);

        log.info("Получено публичное событие {}", eventId);
        return mapToEventFullDto(event, confirmed, rating);
    }

    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long eventId) {
        var event = getEventOrThrow(eventId);
        var confirmed = getConfirmedRequests(eventId);
        var rating = getRating(eventId);

        log.info("Получено событие {}", eventId);
        return mapToEventFullDto(event, confirmed, rating);
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> findByUserId(Long userId, Pageable pageable) {
        if (!userIsExist(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        var events = repository.findAllByinitiator(userId, pageable);
        var eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Double> ratings = getRatings(eventIds);

        return events.stream()
                .map(event -> EventMapperDep.eventToShortDto(
                        event,
                        getConfirmedRequests(event.getId()),
                        ratings.getOrDefault(event.getId(), 0.0)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> findPublicEventsWithFilter(EventsFilter filter, int from, int size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        return findEventsWithFilterInternal(
                filter,
                pageable,
                false,
                (event, ratings) -> EventMapperDep.eventToShortDto(
                        event,
                        getConfirmedRequests(event.getId()),
                        ratings.getOrDefault(event.getId(), 0.0)
                )
        );
    }

    @Transactional(readOnly = true)
    public List<EventFullDto> findAdminEventsWithFilter(EventsFilter filter, Pageable pageable) {
        return findEventsWithFilterInternal(
                filter,
                pageable,
                true,
                (event, ratings) -> EventMapperDep.eventToFullDto(
                        event,
                        getConfirmedRequests(event.getId()),
                        ratings.getOrDefault(event.getId(), 0.0)
                )
        );
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getRecommendations(Long userId, int maxResults) {
        var recs = analyzerClient.getRecommendationsForUser(userId, maxResults).toList();
        if (recs.isEmpty()) {
            return List.of();
        }

        var ids = recs.stream().map(RecommendedEventProto::getEventId).toList();
        var events = repository.findAllById(ids);

        Map<Long, Double> ratings = recs.stream()
                .collect(Collectors.toMap(
                        RecommendedEventProto::getEventId,
                        RecommendedEventProto::getScore,
                        (a, b) -> a
                ));

        return events.stream()
                .map(e -> EventMapperDep.eventToShortDto(
                        e,
                        getConfirmedRequests(e.getId()),
                        ratings.getOrDefault(e.getId(), 0.0)
                ))
                .toList();
    }

    private EventFullDto mapToEventFullDto(Event event, Long confirmed, double rating) {
        return mapper.toDto(event).toBuilder()
                .confirmedRequests(confirmed)
                .rating(rating)
                .build();
    }

    private EventState mapActionToState(EventStateAction action, boolean isAdmin) {
        if (action == null) {
            return null;
        }

        if (isAdmin) {
            return switch (action) {
                case PUBLISH_EVENT -> EventState.PUBLISHED;
                case REJECT_EVENT -> EventState.CANCELED;
                default -> null;
            };
        } else {
            return switch (action) {
                case SEND_TO_REVIEW -> EventState.PENDING;
                case CANCEL_REVIEW -> EventState.CANCELED;
                default -> null;
            };
        }
    }

    private <T> List<T> findEventsWithFilterInternal(
            EventsFilter filter,
            Pageable pageable,
            Boolean forAdmin,
            java.util.function.BiFunction<Event, Map<Long, Double>, T> mapperFn
    ) {
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

        List<Event> events = eventsPage.getContent();
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Double> ratings = getRatings(eventIds);

        List<T> result = events.stream()
                .map(e -> mapperFn.apply(e, ratings))
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

    private void validateEventDate(LocalDateTime newDate, EventStateAction action, EventState currentState, Event event) {
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

    private double getRating(long eventId) {
        return analyzerClient.getInteractionsCount(List.of(eventId))
                .findFirst()
                .map(RecommendedEventProto::getScore)
                .orElse(0.0);
    }

    private Map<Long, Double> getRatings(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }
        return analyzerClient.getInteractionsCount(eventIds)
                .collect(Collectors.toMap(
                        RecommendedEventProto::getEventId,
                        RecommendedEventProto::getScore,
                        (a, b) -> a
                ));
    }
}
