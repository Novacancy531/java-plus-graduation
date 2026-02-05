package ru.practicum.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.practicum.repository.UserEventInteractionRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InteractionsCountService {

    private static final Logger log = LoggerFactory.getLogger(InteractionsCountService.class);

    private final UserEventInteractionRepository repo;

    public InteractionsCountService(UserEventInteractionRepository repo) {
        this.repo = repo;
    }

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
