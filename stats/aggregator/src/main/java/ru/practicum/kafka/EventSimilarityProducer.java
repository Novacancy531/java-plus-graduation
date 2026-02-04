package ru.practicum.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.service.AggregatorKafkaProperties;
import ru.practicum.util.AvroSerializer;

@Component
public class EventSimilarityProducer {

    private final KafkaProducer<String, byte[]> producer;
    private final AggregatorKafkaProperties props;

    public EventSimilarityProducer(KafkaProducer<String, byte[]> producer,
                                   AggregatorKafkaProperties props) {
        this.producer = producer;
        this.props = props;
    }

    public void send(EventSimilarityAvro event) {
        byte[] payload = AvroSerializer.serialize(event);
        String key = event.getEventA() + ":" + event.getEventB();

        ProducerRecord<String, byte[]> record =
                new ProducerRecord<>(props.getSimilarityTopic(), key, payload);

        producer.send(record);
    }
}
