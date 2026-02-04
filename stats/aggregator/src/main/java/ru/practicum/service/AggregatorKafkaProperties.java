package ru.practicum.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka")
public class AggregatorKafkaProperties {
    private String bootstrapServers;
    private String consumerGroup;
    private String userActionsTopic;
    private String similarityTopic;
    private String autoOffsetReset;

    public String getBootstrapServers() { return bootstrapServers; }
    public void setBootstrapServers(String bootstrapServers) { this.bootstrapServers = bootstrapServers; }

    public String getConsumerGroup() { return consumerGroup; }
    public void setConsumerGroup(String consumerGroup) { this.consumerGroup = consumerGroup; }

    public String getUserActionsTopic() { return userActionsTopic; }
    public void setUserActionsTopic(String userActionsTopic) { this.userActionsTopic = userActionsTopic; }

    public String getSimilarityTopic() { return similarityTopic; }
    public void setSimilarityTopic(String similarityTopic) { this.similarityTopic = similarityTopic; }

    public String getAutoOffsetReset() { return autoOffsetReset; }
    public void setAutoOffsetReset(String autoOffsetReset) { this.autoOffsetReset = autoOffsetReset; }
}
