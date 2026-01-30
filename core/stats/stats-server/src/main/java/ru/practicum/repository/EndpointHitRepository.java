package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ViewStatsDto;
import ru.practicum.filter.StatsFilter;
import ru.practicum.model.EndpointHit;

import java.util.List;


@Repository
public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {

    @Query("SELECT new ru.practicum.ViewStatsDto(eh.app, eh.uri, COUNT(eh.ip)) " +
            "FROM EndpointHit eh " +
            "WHERE eh.created BETWEEN :#{#filter.start} AND :#{#filter.end} " +
            "AND (:#{#filter.uris} IS NULL OR eh.uri IN :#{#filter.uris}) " +
            "GROUP BY eh.app, eh.uri " +
            "ORDER BY COUNT(eh.ip) DESC")
    List<ViewStatsDto> findStatsByNonUnique(@Param("filter") StatsFilter filter);

    @Query("SELECT new ru.practicum.ViewStatsDto(eh.app, eh.uri, COUNT(DISTINCT eh.ip)) " +
            "FROM EndpointHit eh " +
            "WHERE eh.created BETWEEN :#{#filter.start} AND :#{#filter.end} " +
            "AND (:#{#filter.uris} IS NULL OR eh.uri IN :#{#filter.uris}) " +
            "GROUP BY eh.app, eh.uri " +
            "ORDER BY COUNT(DISTINCT eh.ip) DESC")
    List<ViewStatsDto> findStatsByUnique(@Param("filter") StatsFilter filter);
}
