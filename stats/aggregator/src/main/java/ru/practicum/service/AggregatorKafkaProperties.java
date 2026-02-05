package ru.practicum.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kafka")
public class AggregatorKafkaProperties {
    private String bootstrapServers;
    private String consumerGroup;
    private String userActionsTopic;
    private String similarityTopic;
    private String autoOffsetReset;
    private long pollTimeoutMs = 500;
}
