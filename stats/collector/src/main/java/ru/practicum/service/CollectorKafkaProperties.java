package ru.practicum.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka")
public class CollectorKafkaProperties {

    private String bootstrapServers;
    private Topics topics = new Topics();

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public Topics getTopics() {
        return topics;
    }

    public void setTopics(Topics topics) {
        this.topics = topics;
    }

    public static class Topics {
        private String userActions;

        public String getUserActions() {
            return userActions;
        }

        public void setUserActions(String userActions) {
            this.userActions = userActions;
        }
    }
}
