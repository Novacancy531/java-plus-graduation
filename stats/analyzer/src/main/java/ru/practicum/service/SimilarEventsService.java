package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.repository.EventSimilarityRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimilarEventsService {

    private final EventSimilarityRepository repo;

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
