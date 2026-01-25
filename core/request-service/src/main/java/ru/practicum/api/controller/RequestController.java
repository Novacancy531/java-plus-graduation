package ru.practicum.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.api.RequestControllerApi;
import ru.practicum.constant.RequestStatus;
import ru.practicum.domain.service.RequestService;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class RequestController implements RequestControllerApi {
    private final RequestService service;

    @Override
    public List<ParticipationRequestDto> findByUser(Long userId) {
        return service.getRequestsByUser(userId);
    }

    @Override
    public ParticipationRequestDto create(Long userId, Long eventId) {
        return service.create(userId, eventId);
    }

    @Override
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        return service.cancelRequest(userId, requestId);
    }

    @Override
    public Long countByEventIdAndStatus(long eventId, RequestStatus requestStatus) {
        return service.countByEventIdAndStatus(eventId, requestStatus);
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(Long eventId, Long ownerId, EventRequestStatusUpdateRequest updateDto) {
        return service.updateRequestStatus(ownerId, eventId, updateDto);
    }

    @Override
    public List<ParticipationRequestDto> getRequestsForEventOwner(Long eventId, Long ownerId) {
        return service.getRequestsForEventOwner(ownerId, eventId);
    }

    @Override
    public List<ParticipationRequestDto> findForEvent(Long userId, Long eventId) {
        return service.getRequestsForEventOwner(userId, eventId);
    }

    @Override
    public EventRequestStatusUpdateResult update(Long userId, Long eventId, EventRequestStatusUpdateRequest dto) {
        return service.updateRequestStatus(userId, eventId, dto);
    }
}
