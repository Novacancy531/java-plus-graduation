package ru.practicum.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.service.AggregatorKafkaProperties;
import ru.practicum.util.AvroSerializer;

@Component
public class EventSimilarityProducer {

    private static final Logger log = LoggerFactory.getLogger(EventSimilarityProducer.class);

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
        log.info("Aggregator: send similarity key={} eventA={} eventB={} score={} topic={}",
                key, event.getEventA(), event.getEventB(), event.getScore(), props.getSimilarityTopic());

        ProducerRecord<String, byte[]> record =
                new ProducerRecord<>(props.getSimilarityTopic(), key, payload);

        producer.send(record);
    }
}
