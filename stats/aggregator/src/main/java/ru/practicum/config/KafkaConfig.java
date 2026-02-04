package ru.practicum.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.service.AggregatorKafkaProperties;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties(AggregatorKafkaProperties.class)
public class KafkaConfig {

    @Bean
    public KafkaProducer<String, byte[]> kafkaProducer(AggregatorKafkaProperties props) {
        Properties p = new Properties();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getBootstrapServers());
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        p.put(ProducerConfig.ACKS_CONFIG, "all");
        p.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        return new KafkaProducer<>(p);
    }

    @Bean
    public KafkaConsumer<String, byte[]> kafkaConsumer(AggregatorKafkaProperties props) {
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getBootstrapServers());
        p.put(ConsumerConfig.GROUP_ID_CONFIG, props.getConsumerGroup());
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, props.getAutoOffsetReset());
        p.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return new KafkaConsumer<>(p);
    }

    @Bean
    public KafkaProducerShutdown kafkaProducerShutdown(KafkaProducer<String, byte[]> producer) {
        return new KafkaProducerShutdown(producer);
    }

    @Bean
    public KafkaConsumerShutdown kafkaConsumerShutdown(KafkaConsumer<String, byte[]> consumer) {
        return new KafkaConsumerShutdown(consumer);
    }
}
