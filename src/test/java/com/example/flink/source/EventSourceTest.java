package com.example.flink.source;

import com.example.flink.config.KafkaConfig;
import com.example.flink.model.UserEvent;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EventSource class.
 * 
 * Tests verify:
 * - Proper initialization with valid parameters
 * - Validation of required parameters
 * - Integration with KafkaConfig
 * - DataStream creation
 */
class EventSourceTest {
    
    @Test
    void testConstructorWithValidParameters() {
        // Given
        String bootstrapServers = "localhost:9092";
        String topic = "test-topic";
        String groupId = "test-group";
        
        // When
        EventSource eventSource = new EventSource(bootstrapServers, topic, groupId);
        
        // Then
        assertNotNull(eventSource);
        assertEquals(bootstrapServers, eventSource.getKafkaBootstrapServers());
        assertEquals(topic, eventSource.getTopicName());
        assertEquals(groupId, eventSource.getConsumerGroupId());
    }
    
    @Test
    void testConstructorWithKafkaConfig() {
        // Given
        KafkaConfig kafkaConfig = new KafkaConfig();
        kafkaConfig.setBootstrapServers("localhost:9092");
        kafkaConfig.setTopic("test-topic");
        kafkaConfig.setConsumerGroupId("test-group");
        
        // When
        EventSource eventSource = new EventSource(kafkaConfig);
        
        // Then
        assertNotNull(eventSource);
        assertEquals("localhost:9092", eventSource.getKafkaBootstrapServers());
        assertEquals("test-topic", eventSource.getTopicName());
        assertEquals("test-group", eventSource.getConsumerGroupId());
    }
    
    @Test
    void testConstructorWithNullBootstrapServers() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            new EventSource(null, "test-topic", "test-group");
        });
    }
    
    @Test
    void testConstructorWithEmptyBootstrapServers() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            new EventSource("", "test-topic", "test-group");
        });
    }
    
    @Test
    void testConstructorWithNullTopic() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            new EventSource("localhost:9092", null, "test-group");
        });
    }
    
    @Test
    void testConstructorWithEmptyTopic() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            new EventSource("localhost:9092", "", "test-group");
        });
    }
    
    @Test
    void testConstructorWithNullGroupId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            new EventSource("localhost:9092", "test-topic", null);
        });
    }
    
    @Test
    void testConstructorWithEmptyGroupId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            new EventSource("localhost:9092", "test-topic", "");
        });
    }
    
    @Test
    void testCreateEventStreamReturnsDataStream() {
        // Given
        EventSource eventSource = new EventSource("localhost:9092", "test-topic", "test-group");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // When
        DataStream<UserEvent> eventStream = eventSource.createEventStream(env);
        
        // Then
        assertNotNull(eventStream);
        // Verify the stream is properly configured
        assertNotNull(eventStream.getType());
        assertEquals(UserEvent.class, eventStream.getType().getTypeClass());
    }
    
    @Test
    void testToString() {
        // Given
        EventSource eventSource = new EventSource("localhost:9092", "test-topic", "test-group");
        
        // When
        String result = eventSource.toString();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("localhost:9092"));
        assertTrue(result.contains("test-topic"));
        assertTrue(result.contains("test-group"));
    }
    
    @Test
    void testSerializable() {
        // Given
        EventSource eventSource = new EventSource("localhost:9092", "test-topic", "test-group");
        
        // Then - verify it implements Serializable (compile-time check)
        assertTrue(eventSource instanceof java.io.Serializable);
    }
}
