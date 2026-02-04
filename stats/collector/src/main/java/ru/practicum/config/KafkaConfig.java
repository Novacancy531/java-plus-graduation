package ru.practicum.config;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.service.CollectorKafkaProperties;

import java.util.Properties;

@Configuration
public class KafkaConfig {

    @Bean
    public KafkaProducer<String, byte[]> kafkaProducer(CollectorKafkaProperties props) {
        Properties p = new Properties();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getBootstrapServers());
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        return new KafkaProducer<>(p);
    }

    @Bean
    public KafkaProducerShutdown kafkaProducerShutdown(
            KafkaProducer<String, byte[]> producer
    ) {
        return new KafkaProducerShutdown(producer);
    }
}
