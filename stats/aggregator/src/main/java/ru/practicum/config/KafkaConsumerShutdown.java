package ru.practicum.config;

import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class KafkaConsumerShutdown {

    private final KafkaConsumer<String, byte[]> consumer;

    public KafkaConsumerShutdown(KafkaConsumer<String, byte[]> consumer) {
        this.consumer = consumer;
    }

    @PreDestroy
    public void shutdown() {
        consumer.wakeup();
        consumer.close();
    }
}
