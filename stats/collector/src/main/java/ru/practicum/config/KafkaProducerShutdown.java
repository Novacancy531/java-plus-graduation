package ru.practicum.config;

import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.producer.KafkaProducer;

public class KafkaProducerShutdown {

    private final KafkaProducer<String, byte[]> producer;

    public KafkaProducerShutdown(KafkaProducer<String, byte[]> producer) {
        this.producer = producer;
    }

    @PreDestroy
    public void shutdown() {
        producer.flush();
        producer.close();
    }
}
