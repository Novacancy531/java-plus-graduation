package ru.practicum.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import ru.practicum.kafka.mapper.UserActionAvroMapper;
import ru.practicum.model.UserEventInteraction;
import ru.practicum.repository.UserEventInteractionRepository;
import ru.practicum.util.AvroDeserializer;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserActionsConsumer {

    private final UserEventInteractionRepository repo;
    private final UserActionAvroMapper mapper;

    @KafkaListener(
            topics = "${kafka.topics.user-actions}",
            groupId = "stats-analyzer-user-actions",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, byte[]> record) {
        log.info("Analyzer: received user-action key={} topic={} offset={}",
                record.key(), record.topic(), record.offset());
        UserActionAvro avro = AvroDeserializer.deserialize(record.value(), new UserActionAvro());
        UserEventInteraction incoming = mapper.map(avro);

        Optional<UserEventInteraction> existing =
                repo.findByUserIdAndEventId(incoming.getUserId(), incoming.getEventId());

        if (existing.isEmpty()) {
            repo.save(incoming);
            return;
        }

        UserEventInteraction current = existing.get();
        if (incoming.getWeight() > current.getWeight()) {
            current.setWeight(incoming.getWeight());
            current.setLastTs(incoming.getLastTs());
            repo.save(current);
        } else if (incoming.getLastTs().isAfter(current.getLastTs())) {
            current.setLastTs(incoming.getLastTs());
            repo.save(current);
        }
    }
}
