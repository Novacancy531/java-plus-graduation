package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.service.AggregatorKafkaProperties;
import ru.practicum.util.AvroSerializer;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventSimilarityProducer {

    private final KafkaProducer<String, byte[]> producer;
    private final AggregatorKafkaProperties props;

    public void send(EventSimilarityAvro event) {
        byte[] payload = AvroSerializer.serialize(event);
        String key = event.getEventA() + ":" + event.getEventB();
        log.info("Aggregator: send similarity key={} eventA={} eventB={} score={} topic={}",
                key, event.getEventA(), event.getEventB(), event.getScore(), props.getSimilarityTopic());

        ProducerRecord<String, byte[]> record =
                new ProducerRecord<>(props.getSimilarityTopic(), key, payload);

        producer.send(record);
    }
}
