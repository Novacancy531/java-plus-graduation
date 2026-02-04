package ru.practicum.service;

import org.springframework.stereotype.Service;
import ru.practicum.repository.EventSimilarityRepository;
import ru.practicum.repository.UserEventInteractionRepository;
import ru.practicum.model.UserEventInteraction;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserRecommendationsService {

    private final UserEventInteractionRepository interactionsRepo;
    private final EventSimilarityRepository similarityRepo;

    public UserRecommendationsService(UserEventInteractionRepository interactionsRepo,
                                      EventSimilarityRepository similarityRepo) {
        this.interactionsRepo = interactionsRepo;
        this.similarityRepo = similarityRepo;
    }

    public List<Rec> recommend(long userId, int maxResults) {
        List<UserEventInteraction> recent = interactionsRepo.findTop50ByUserIdOrderByLastTsDesc(userId);
        if (recent.isEmpty()) {
            return List.of();
        }

        Map<Long, Double> weightsByEvent = recent.stream()
                .collect(Collectors.toMap(UserEventInteraction::getEventId, UserEventInteraction::getWeight, Math::max));

        Set<Long> seen = weightsByEvent.keySet();

        int probe = Math.max(maxResults * 20, 50);
        Map<Long, Double> bestSimilarity = new HashMap<>();

        for (UserEventInteraction r : recent) {
            List<Object[]> a = similarityRepo.findSimilarFromA(r.getEventId(), userId, probe);
            List<Object[]> b = similarityRepo.findSimilarFromB(r.getEventId(), userId, probe);

            for (Object[] row : a) {
                long candidate = ((Number) row[0]).longValue();
                double sim = ((Number) row[1]).doubleValue();
                if (!seen.contains(candidate)) {
                    bestSimilarity.merge(candidate, sim, Math::max);
                }
            }
            for (Object[] row : b) {
                long candidate = ((Number) row[0]).longValue();
                double sim = ((Number) row[1]).doubleValue();
                if (!seen.contains(candidate)) {
                    bestSimilarity.merge(candidate, sim, Math::max);
                }
            }
        }

        List<Map.Entry<Long, Double>> candidates = bestSimilarity.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(probe)
                .toList();

        List<Rec> scored = new ArrayList<>();

        for (Map.Entry<Long, Double> c : candidates) {
            long candidateId = c.getKey();
            double predicted = predict(candidateId, weightsByEvent);
            if (predicted > 0) {
                scored.add(new Rec(candidateId, predicted));
            }
        }

        return scored.stream()
                .sorted(Comparator.comparingDouble(Rec::score).reversed())
                .limit(maxResults)
                .toList();
    }

    private double predict(long candidateId, Map<Long, Double> weightsByEvent) {
        double num = 0.0;
        double den = 0.0;

        for (Map.Entry<Long, Double> e : weightsByEvent.entrySet()) {
            long neighbor = e.getKey();
            double weight = e.getValue();

            Double sim = loadSimilarity(candidateId, neighbor);
            if (sim == null || sim <= 0) {
                continue;
            }

            num += sim * weight;
            den += sim;
        }

        if (den == 0.0) {
            return 0.0;
        }
        return num / den;
    }

    private Double loadSimilarity(long a, long b) {
        long x = Math.min(a, b);
        long y = Math.max(a, b);
        return similarityRepo.findScore(x, y);
    }

    public record Rec(long eventId, double score) {
    }
}
