package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.repository.UserEventInteractionRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InteractionsCountService {

    private final UserEventInteractionRepository repo;

    public Map<Long, Double> getCounts(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            log.debug("Analyzer: getCounts empty eventIds");
            return Map.of();
        }

        log.debug("Analyzer: getCounts eventIdsSize={}", eventIds.size());
        List<Object[]> rows = repo.sumWeightsByEventIds(eventIds.toArray(new Long[0]));
        Map<Long, Double> res = new HashMap<>();
        for (Object[] r : rows) {
            res.put(((Number) r[0]).longValue(), ((Number) r[1]).doubleValue());
        }
        log.debug("Analyzer: getCounts resultSize={}", res.size());
        return res;
    }
}
