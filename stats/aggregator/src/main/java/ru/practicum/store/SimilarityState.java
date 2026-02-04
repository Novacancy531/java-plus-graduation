package ru.practicum.store;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SimilarityState {

    private final Map<Long, Map<Long, Double>> maxWeights = new HashMap<>();
    private final Map<Long, Double> sEvent = new HashMap<>();
    private final Map<Long, Map<Long, Double>> sMin = new HashMap<>();

    public Map<Long, Map<Long, Double>> getMaxWeights() {
        return maxWeights;
    }

    public Map<Long, Double> getsEvent() {
        return sEvent;
    }

    public Map<Long, Map<Long, Double>> getsMin() {
        return sMin;
    }

    public Map<Long, Double> getOrCreateUserWeights(long eventId) {
        return maxWeights.computeIfAbsent(eventId, k -> new HashMap<>());
    }

    public double getSEvent(long eventId) {
        return sEvent.getOrDefault(eventId, 0.0);
    }

    public void addSEvent(long eventId, double delta) {
        sEvent.put(eventId, getSEvent(eventId) + delta);
    }

    public double getSMin(Pair pair) {
        Map<Long, Double> inner = sMin.get(pair.getFirst());
        if (inner == null) {
            return 0.0;
        }
        return inner.getOrDefault(pair.getSecond(), 0.0);
    }

    public void addSMin(Pair pair, double delta) {
        sMin.computeIfAbsent(pair.getFirst(), k -> new HashMap<>())
                .merge(pair.getSecond(), delta, Double::sum);
    }
}
