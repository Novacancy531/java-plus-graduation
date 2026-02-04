package ru.practicum.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.mapper.UserActionAvroMapper;
import ru.practicum.model.UserEventInteraction;
import ru.practicum.repository.UserEventInteractionRepository;
import ru.practicum.service.AnalyzerKafkaProperties;
import ru.practicum.util.AvroDeserializer;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Optional;

@Component
public class UserActionsConsumer {

    private final AnalyzerKafkaProperties props;
    private final UserEventInteractionRepository repo;
    private final UserActionAvroMapper mapper;

    public UserActionsConsumer(AnalyzerKafkaProperties props,
                               UserEventInteractionRepository repo,
                               UserActionAvroMapper mapper) {
        this.props = props;
        this.repo = repo;
        this.mapper = mapper;
    }

    @KafkaListener(
            topics = "${kafka.topics.user-actions}",
            groupId = "stats-analyzer-user-actions",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(byte[] data) {
        UserActionAvro avro = AvroDeserializer.deserialize(data, new UserActionAvro());
        UserEventInteraction incoming = mapper.map(avro);

        Optional<UserEventInteraction> existing = repo.findAll().stream()
                .filter(x -> x.getUserId().equals(incoming.getUserId()) && x.getEventId().equals(incoming.getEventId()))
                .findFirst();

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
