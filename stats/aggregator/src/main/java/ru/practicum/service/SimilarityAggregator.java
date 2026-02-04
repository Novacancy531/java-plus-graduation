package ru.practicum.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.kafka.EventSimilarityProducer;
import ru.practicum.store.Pair;
import ru.practicum.store.SimilarityState;

import java.time.Instant;
import java.util.Map;

@Service
public class SimilarityAggregator {

    private static final Logger log = LoggerFactory.getLogger(SimilarityAggregator.class);

    private final EventSimilarityProducer producer;
    private final SimilarityState state;

    public SimilarityAggregator(EventSimilarityProducer producer, SimilarityState state) {
        this.producer = producer;
        this.state = state;
    }

    public void onAction(UserActionAvro action) {
        long userId = action.getUserId();
        long eventA = action.getEventId();
        Instant ts = action.getTimestamp();

        double newW = toWeight(action.getActionType());

        log.debug("action received userId={} eventId={} type={} weight={}",
                userId, eventA, action.getActionType(), newW);

        Map<Long, Double> perUserA = state.getOrCreateUserWeights(eventA);
        double oldW = perUserA.getOrDefault(userId, 0.0);

        if (newW <= oldW) {
            log.debug("skip action userId={} eventId={} oldWeight={} newWeight={}",
                    userId, eventA, oldW, newW);
            return;
        }

        perUserA.put(userId, newW);

        double deltaA = newW - oldW;
        state.addSEvent(eventA, deltaA);
        double sA = state.getSEvent(eventA);

        log.debug("update S_event eventId={} delta={} S_event={}",
                eventA, deltaA, sA);

        for (Map.Entry<Long, Map<Long, Double>> entry : state.getMaxWeights().entrySet()) {
            long eventB = entry.getKey();
            if (eventB == eventA) {
                continue;
            }

            double wB = entry.getValue().getOrDefault(userId, 0.0);
            if (wB == 0.0) {
                continue;
            }

            double deltaMin = Math.min(newW, wB) - Math.min(oldW, wB);
            if (deltaMin != 0.0) {
                state.addSMin(new Pair(eventA, eventB), deltaMin);
            }

            double sB = state.getSEvent(eventB);
            if (sA == 0.0 || sB == 0.0) {
                continue;
            }

            Pair p = new Pair(eventA, eventB);
            double sm = state.getSMin(p);
            double score = sm / (sA * sB);

            log.debug("similarity recalculated A={} B={} Smin={} score={}",
                    p.getFirst(), p.getSecond(), sm, score);

            EventSimilarityAvro msg = EventSimilarityAvro.newBuilder()
                    .setEventA(p.getFirst())
                    .setEventB(p.getSecond())
                    .setScore(score)
                    .setTimestamp(ts)
                    .build();

            producer.send(msg);

            log.debug("similarity sent A={} B={} score={}",
                    p.getFirst(), p.getSecond(), score);
        }
    }

    private double toWeight(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}
