package ru.practicum.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kafka")
public class CollectorKafkaProperties {

    private String bootstrapServers;
    private Topics topics = new Topics();

    @Data
    public static class Topics {
        private String userActions;
    }
}
