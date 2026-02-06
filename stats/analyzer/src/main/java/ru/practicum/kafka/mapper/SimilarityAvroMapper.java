package ru.practicum.kafka.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.model.EventSimilarity;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.OffsetDateTime;

@Component
public class SimilarityAvroMapper {

    public EventSimilarity map(EventSimilarityAvro avro) {
        long a = Math.min(avro.getEventA(), avro.getEventB());
        long b = Math.max(avro.getEventA(), avro.getEventB());

        EventSimilarity e = new EventSimilarity();
        e.setEventA(a);
        e.setEventB(b);
        e.setScore(avro.getScore());
        e.setUpdatedAt(
                OffsetDateTime.ofInstant(
                        avro.getTimestamp(),
                        OffsetDateTime.now().getOffset()
                )
        );
        return e;
    }
}
