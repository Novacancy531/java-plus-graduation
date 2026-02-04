package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.UserEventInteraction;

import java.util.List;
import java.util.Optional;

public interface UserEventInteractionRepository extends JpaRepository<UserEventInteraction, Long> {

    Optional<UserEventInteraction> findByUserIdAndEventId(Long userId, Long eventId);

    @Query(value = """
        select event_id, sum(weight) as score
        from user_event_interaction
        where event_id = any(:eventIds)
        group by event_id
        """, nativeQuery = true)
    List<Object[]> sumWeightsByEventIds(@Param("eventIds") Long[] eventIds);

    List<UserEventInteraction> findTop50ByUserIdOrderByLastTsDesc(Long userId);
}
