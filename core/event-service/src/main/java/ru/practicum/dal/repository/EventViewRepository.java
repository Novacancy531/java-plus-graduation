package ru.practicum.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.dal.entity.EventView;

public interface EventViewRepository extends JpaRepository<EventView, Long> {
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
}
