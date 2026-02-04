package ru.practicum.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.mapper.SimilarityAvroMapper;
import ru.practicum.model.EventSimilarity;
import ru.practicum.repository.EventSimilarityRepository;
import ru.practicum.util.AvroDeserializer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.Optional;

@Component
public class EventsSimilarityConsumer {

    private final EventSimilarityRepository repo;
    private final SimilarityAvroMapper mapper;

    public EventsSimilarityConsumer(EventSimilarityRepository repo,
                                    SimilarityAvroMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @KafkaListener(
            topics = "${kafka.topics.events-similarity}",
            groupId = "stats-analyzer-events-similarity",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(byte[] data) {
        EventSimilarityAvro avro = AvroDeserializer.deserialize(data, new EventSimilarityAvro());
        EventSimilarity incoming = mapper.map(avro);

        Optional<EventSimilarity> existing = repo.findAll().stream()
                .filter(x -> x.getEventA().equals(incoming.getEventA()) && x.getEventB().equals(incoming.getEventB()))
                .findFirst();

        if (existing.isEmpty()) {
            repo.save(incoming);
            return;
        }

        EventSimilarity current = existing.get();
        current.setScore(incoming.getScore());
        current.setUpdatedAt(incoming.getUpdatedAt());
        repo.save(current);
    }
}
