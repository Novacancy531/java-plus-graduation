package ru.practicum.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.practicum.repository.EventSimilarityRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SimilarEventsService {

    private static final Logger log = LoggerFactory.getLogger(SimilarEventsService.class);

    private final EventSimilarityRepository repo;

    public SimilarEventsService(EventSimilarityRepository repo) {
        this.repo = repo;
    }

    public List<Rec> getSimilar(long eventId, long userId, int maxResults) {
        log.debug("Analyzer: getSimilar eventId={} userId={} maxResults={}", eventId, userId, maxResults);
        List<Object[]> a = repo.findSimilarFromA(eventId, userId, maxResults);
        List<Object[]> b = repo.findSimilarFromB(eventId, userId, maxResults);

        List<Rec> res = new ArrayList<>(a.size() + b.size());
        a.forEach(r -> res.add(new Rec(((Number) r[0]).longValue(), ((Number) r[1]).doubleValue())));
        b.forEach(r -> res.add(new Rec(((Number) r[0]).longValue(), ((Number) r[1]).doubleValue())));

        List<Rec> result = res.stream()
                .sorted(Comparator.comparingDouble(Rec::score).reversed())
                .limit(maxResults)
                .toList();
        log.debug("Analyzer: getSimilar eventId={} userId={} resultSize={}", eventId, userId, result.size());
        return result;
    }

    public record Rec(long eventId, double score) {
    }
}
