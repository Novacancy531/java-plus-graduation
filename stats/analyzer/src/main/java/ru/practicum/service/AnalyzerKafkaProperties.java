package ru.practicum.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kafka")
public class AnalyzerKafkaProperties {

    private String bootstrapServers;
    private Topics topics = new Topics();

    @Data
    public static class Topics {
        private String userActions;
        private String eventsSimilarity;
    }
}
