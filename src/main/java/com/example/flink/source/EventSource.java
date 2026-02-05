package com.example.flink.source;

import com.example.flink.config.KafkaConfig;
import com.example.flink.deserializer.UserEventDeserializer;
import com.example.flink.model.UserEvent;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * EventSource creates a Kafka source for consuming user tracking events.
 * 
 * This class is responsible for:
 * - Creating a KafkaSource with proper configuration
 * - Setting up Kafka consumer parameters (bootstrap servers, topic, group id)
 * - Integrating the UserEventDeserializer for JSON deserialization
 * - Creating a Flink DataStream from the Kafka source
 * 
 * Requirements: 1.1, 1.2
 */
public class EventSource implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(EventSource.class);
    
    private final String kafkaBootstrapServers;
    private final String topicName;
    private final String consumerGroupId;
    
    /**
     * Constructor for EventSource.
     * 
     * @param kafkaBootstrapServers Kafka bootstrap servers (e.g., "localhost:9092")
     * @param topicName Kafka topic name to consume from
     * @param consumerGroupId Kafka consumer group ID
     */
    public EventSource(String kafkaBootstrapServers, String topicName, String consumerGroupId) {
        if (kafkaBootstrapServers == null || kafkaBootstrapServers.isEmpty()) {
            throw new IllegalArgumentException("Kafka bootstrap servers cannot be null or empty");
        }
        if (topicName == null || topicName.isEmpty()) {
            throw new IllegalArgumentException("Topic name cannot be null or empty");
        }
        if (consumerGroupId == null || consumerGroupId.isEmpty()) {
            throw new IllegalArgumentException("Consumer group ID cannot be null or empty");
        }
        
        this.kafkaBootstrapServers = kafkaBootstrapServers;
        this.topicName = topicName;
        this.consumerGroupId = consumerGroupId;
        
        logger.info("EventSource initialized with bootstrapServers={}, topic={}, groupId={}", 
                    kafkaBootstrapServers, topicName, consumerGroupId);
    }
    
    /**
     * Constructor that accepts KafkaConfig object.
     * 
     * @param kafkaConfig Kafka configuration object
     */
    public EventSource(KafkaConfig kafkaConfig) {
        this(kafkaConfig.getBootstrapServers(), 
             kafkaConfig.getTopic(), 
             kafkaConfig.getConsumerGroupId());
    }
    
    /**
     * Creates a Flink DataStream of UserEvent from Kafka.
     * 
     * This method:
     * 1. Builds a KafkaSource with the configured parameters
     * 2. Sets up the UserEventDeserializer for JSON deserialization
     * 3. Configures the source to start from the latest offset
     * 4. Creates a DataStream from the source with no watermarks (processing time)
     * 
     * @param env Flink StreamExecutionEnvironment
     * @return DataStream of UserEvent objects
     */
    public DataStream<UserEvent> createEventStream(StreamExecutionEnvironment env) {
        logger.info("Creating Kafka event stream from topic: {}", topicName);
        
        // Build KafkaSource with configuration
        KafkaSource<UserEvent> kafkaSource = KafkaSource.<UserEvent>builder()
            .setBootstrapServers(kafkaBootstrapServers)
            .setTopics(topicName)
            .setGroupId(consumerGroupId)
            .setValueOnlyDeserializer(new UserEventDeserializer())
            .setStartingOffsets(OffsetsInitializer.latest())
            .build();
        
        logger.info("KafkaSource configured successfully");
        
        // Create DataStream from KafkaSource
        // Using WatermarkStrategy.noWatermarks() for processing time semantics
        DataStream<UserEvent> eventStream = env.fromSource(
            kafkaSource,
            WatermarkStrategy.noWatermarks(),
            "Kafka Source"
        );
        
        logger.info("Event stream created successfully");
        
        return eventStream;
    }
    
    /**
     * Get the Kafka bootstrap servers.
     * 
     * @return Kafka bootstrap servers
     */
    public String getKafkaBootstrapServers() {
        return kafkaBootstrapServers;
    }
    
    /**
     * Get the topic name.
     * 
     * @return Kafka topic name
     */
    public String getTopicName() {
        return topicName;
    }
    
    /**
     * Get the consumer group ID.
     * 
     * @return Kafka consumer group ID
     */
    public String getConsumerGroupId() {
        return consumerGroupId;
    }
    
    @Override
    public String toString() {
        return "EventSource{" +
                "kafkaBootstrapServers='" + kafkaBootstrapServers + '\'' +
                ", topicName='" + topicName + '\'' +
                ", consumerGroupId='" + consumerGroupId + '\'' +
                '}';
    }
}
