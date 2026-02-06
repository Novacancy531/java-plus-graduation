package ru.practicum.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import ru.practicum.kafka.mapper.SimilarityAvroMapper;
import ru.practicum.model.EventSimilarity;
import ru.practicum.repository.EventSimilarityRepository;
import ru.practicum.util.AvroDeserializer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventsSimilarityConsumer {

    private final EventSimilarityRepository repo;
    private final SimilarityAvroMapper mapper;

    @KafkaListener(
            topics = "${kafka.topics.events-similarity}",
            groupId = "stats-analyzer-events-similarity",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, byte[]> record) {
        log.info("Analyzer: received similarity key={} topic={} offset={}",
                record.key(), record.topic(), record.offset());
        EventSimilarityAvro avro = AvroDeserializer.deserialize(record.value(), new EventSimilarityAvro());
        EventSimilarity incoming = mapper.map(avro);

        Optional<EventSimilarity> existing =
                repo.findByEventAAndEventB(incoming.getEventA(), incoming.getEventB());

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
