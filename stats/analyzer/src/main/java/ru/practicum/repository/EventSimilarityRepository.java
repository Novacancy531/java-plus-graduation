package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.EventSimilarity;

import java.util.List;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    @Query(value = """
        select es.event_b, es.score
        from event_similarity es
        where es.event_a = :eventId
          and not exists (
            select 1
            from user_event_interaction uei
            where uei.user_id = :userId
              and uei.event_id = es.event_b
          )
        order by es.score desc
        limit :maxResults
        """, nativeQuery = true)
    List<Object[]> findSimilarFromA(@Param("eventId") long eventId,
                                    @Param("userId") long userId,
                                    @Param("maxResults") int maxResults);

    @Query(value = """
        select es.event_a, es.score
        from event_similarity es
        where es.event_b = :eventId
          and not exists (
            select 1
            from user_event_interaction uei
            where uei.user_id = :userId
              and uei.event_id = es.event_a
          )
        order by es.score desc
        limit :maxResults
        """, nativeQuery = true)
    List<Object[]> findSimilarFromB(@Param("eventId") long eventId,
                                    @Param("userId") long userId,
                                    @Param("maxResults") int maxResults);

    @Query(value = """
    select score
    from event_similarity
    where event_a = :a and event_b = :b
    """, nativeQuery = true)
    Double findScore(@Param("a") long a, @Param("b") long b);
}
