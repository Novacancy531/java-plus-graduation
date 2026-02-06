package ru.practicum.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.client.EventServiceFacade;
import ru.practicum.client.UserServiceFacade;
import ru.practicum.collector.CollectorClient;
import ru.practicum.constant.EventState;
import ru.practicum.constant.RequestStatus;
import ru.practicum.dal.entity.Request;
import ru.practicum.dal.repository.RequestRepository;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ConditionsException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestService {

    private final UserServiceFacade userServiceClient;
    private final EventServiceFacade eventServiceClient;
    private final RequestRepository repository;
    private final RequestMapper mapper;
    private final TransactionTemplate transactionTemplate;
    private final CollectorClient collectorClient;


    public ParticipationRequestDto create(Long userId, Long eventId) {

        var requester = getUserOrThrow(userId);
        var event = getPublishedEventOrThrow(eventId);

        if (Objects.equals(event.getInitiator(), userId)) {
            throw new ConflictException("Нельзя подать заявку на своё мероприятие");
        }

        var limit = event.getParticipantLimit();
        var isConfirmed = !event.getRequestModeration() || (limit != null && limit == 0L);

        var saved = transactionTemplate.execute(status -> {

            if (repository.findByEventIdAndRequesterId(eventId, userId).isPresent()) {
                throw new ConflictException("Запрос уже существует");
            }

            if (limit != null && limit > 0) {
                long confirmedCount = repository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
                if (confirmedCount >= limit) {
                    throw new ConflictException("Достигнут лимит участников");
                }
            }

            var request = Request.newRequest(eventId, requester.getId(), isConfirmed);
            return repository.save(request);
        });

        collectorClient.register(userId, eventId);

        log.info("Создан запрос, id = {}", saved.getId());
        return mapper.toDto(saved);
    }



    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByUser(Long userId) {
        if (!userServiceClient.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        return repository.findByRequesterId(userId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        var request = repository.findById(requestId).orElseThrow(() -> new NotFoundException("Заявка не найдена"));

        request.cancelBy(userId);

        var saved = repository.save(request);
        log.info("Отменен запрос id = {}", requestId);
        return mapper.toDto(saved);
    }


    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsForEventOwner(Long ownerId, Long eventId) {
        getEventForOwnerOrThrow(eventId, ownerId,
                "Только владелец мероприятия может просматривать запросы на это мероприятие"
        );
        return repository.findByEventId(eventId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long ownerId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateDto) {

        var event = getEventForOwnerOrThrow(eventId, ownerId,
                "Только владелец мероприятия может изменять статус запроса"
        );

        if (updateDto.getRequestIds() == null || updateDto.getRequestIds().isEmpty()) {
            return new EventRequestStatusUpdateResult(List.of(), List.of());
        }
        if (updateDto.getStatus() == null) {
            throw new ConditionsException("Не указан статус");
        }

        if (updateDto.getStatus() == RequestStatus.CONFIRMED &&
                (!event.getRequestModeration() || event.getParticipantLimit() == 0)) {
            throw new ConditionsException("Подтверждение заявок не требуется");
        }

        Long freeLimit = event.getParticipantLimit();
        if (freeLimit != null && freeLimit > 0) {
            freeLimit = freeLimit - repository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (freeLimit <= 0) {
                throw new ConflictException("Лимит по заявкам на данное событие уже достигнут");
            }
        }

        List<Request> requests =
                repository.findAllByIdInAndStatus(updateDto.getRequestIds(), RequestStatus.PENDING);

        Map<Long, Request> map = requests.stream()
                .collect(Collectors.toMap(Request::getId, r -> r));

        List<Long> notFound = updateDto.getRequestIds().stream()
                .filter(id -> !map.containsKey(id))
                .toList();

        if (!notFound.isEmpty()) {
            throw new ConflictException("Не найдены заявки: " + notFound);
        }

        List<Request> toUpdate = new ArrayList<>();
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (Long id : updateDto.getRequestIds()) {
            Request request = map.get(id);

            if (updateDto.getStatus() == RequestStatus.CONFIRMED) {
                if (freeLimit != null && freeLimit <= 0) {
                    request.reject();
                    rejected.add(mapper.toDto(request));
                    log.info("Заявка {} будет отклонена", request.getId());
                } else {
                    if (freeLimit != null) {
                        freeLimit--;
                    }
                    request.confirm();
                    confirmed.add(mapper.toDto(request));
                    log.info("Заявка {} будет подтверждена", request.getId());
                }
            } else if (updateDto.getStatus() == RequestStatus.REJECTED) {
                request.reject();
                rejected.add(mapper.toDto(request));
            } else {
                throw new ConflictException("Доступны только статусы CONFIRMED или REJECTED");
            }

            toUpdate.add(request);
        }

        if (!toUpdate.isEmpty()) {
            repository.saveAll(toUpdate);
            log.info("Список заявок обновлен");
        }

        if (freeLimit != null && freeLimit == 0 && event.getParticipantLimit() > 0) {
            List<Request> pendingRequests =
                    repository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);
            if (!pendingRequests.isEmpty()) {
                pendingRequests.forEach(Request::reject);
                List<Request> rejectedRequests = repository.saveAll(pendingRequests);
                rejected.addAll(rejectedRequests.stream().map(mapper::toDto).toList());
                log.info("Был достигнут лимит заявок, все оставшиеся PENDING, переведены в REJECTED");
            }
        }

        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }

    public long countByEventIdAndStatus (long eventId, RequestStatus requestStatus) {
        return repository.countByEventIdAndStatus(eventId, requestStatus);
    }

    private EventFullDto getEventOrThrow(Long eventId) {
        EventFullDto event = eventServiceClient.getEventById(eventId);
        if (event == null) {
            throw new NotFoundException("Мероприятие с id=" + eventId + " не найдено");
        }
        return event;
    }

    private UserDto getUserOrThrow(Long userId) {
        UserDto user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        return user;
    }

    private EventFullDto getPublishedEventOrThrow(Long eventId) {
        EventFullDto event = getEventOrThrow(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Подать заявку можно только на опубликованные мероприятия");
        }
        return event;
    }

    private EventFullDto getEventForOwnerOrThrow(Long eventId, Long ownerId, String message) {
        EventFullDto event = getEventOrThrow(eventId);
        if (!Objects.equals(event.getInitiator(), ownerId)) {
            throw new ConditionsException(message);
        }
        return event;
    }
}
