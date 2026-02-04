package ru.practicum.domain.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.collector.CollectorClient;
import ru.practicum.dal.repository.EventViewRepository;
import ru.practicum.exception.ConflictException;

@Service
@RequiredArgsConstructor
public class EventViewService {
    private final EventViewRepository eventViewRepository;
    private final CollectorClient collectorClient;

    @Transactional
    public void likeEvent(Long userId, Long eventId) {
        if (!eventViewRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new ConflictException("Можно лайкать только просмотренное событие");
        }
        collectorClient.like(userId, eventId);
    }
}
