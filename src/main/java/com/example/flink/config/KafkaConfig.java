package com.example.flink.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Kafka configuration
 */
public class KafkaConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("bootstrap-servers")
    private String bootstrapServers;

    @JsonProperty("topic")
    private String topic;

    @JsonProperty("consumer-group-id")
    private String consumerGroupId;

    public KafkaConfig() {
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getConsumerGroupId() {
        return consumerGroupId;
    }

    public void setConsumerGroupId(String consumerGroupId) {
        this.consumerGroupId = consumerGroupId;
    }

    @Override
    public String toString() {
        return "KafkaConfig{" +
                "bootstrapServers='" + bootstrapServers + '\'' +
                ", topic='" + topic + '\'' +
                ", consumerGroupId='" + consumerGroupId + '\'' +
                '}';
    }
}
