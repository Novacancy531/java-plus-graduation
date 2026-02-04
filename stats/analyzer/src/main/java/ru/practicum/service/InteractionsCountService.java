package ru.practicum.service;

import org.springframework.stereotype.Service;
import ru.practicum.repository.UserEventInteractionRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InteractionsCountService {

    private final UserEventInteractionRepository repo;

    public InteractionsCountService(UserEventInteractionRepository repo) {
        this.repo = repo;
    }

    public Map<Long, Double> getCounts(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = repo.sumWeightsByEventIds(eventIds.toArray(new Long[0]));
        Map<Long, Double> res = new HashMap<>();
        for (Object[] r : rows) {
            res.put(((Number) r[0]).longValue(), ((Number) r[1]).doubleValue());
        }
        return res;
    }
}
