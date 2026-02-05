package com.example.flink.deserializer;

import com.example.flink.model.EventParameters;
import com.example.flink.model.UserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserEventDeserializer.
 * Tests specific examples, edge cases, and error conditions.
 */
class UserEventDeserializerTest {
    private UserEventDeserializer deserializer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        deserializer = new UserEventDeserializer();
        deserializer.open(null);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testDeserializeValidEvent() throws IOException {
        // Arrange: Create a valid JSON event
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("event_id", "evt-123");
        eventMap.put("user_id", "user-456");
        eventMap.put("domain", "example.com");
        eventMap.put("event_type", "page_view");
        eventMap.put("event_name", "Home Page");
        eventMap.put("timestamp", 1234567890L);
        eventMap.put("triggerable", true);
        
        Map<String, Object> params = new HashMap<>();
        params.put("utm_source", "google");
        eventMap.put("parameters", params);
        
        String json = objectMapper.writeValueAsString(eventMap);
        byte[] message = json.getBytes();

        // Act: Deserialize the message
        UserEvent event = deserializer.deserialize(message);

        // Assert: Verify the event was deserialized correctly
        assertNotNull(event);
        assertEquals("evt-123", event.getEventId());
        assertEquals("user-456", event.getUserId());
        assertEquals("example.com", event.getDomain());
        assertEquals("page_view", event.getEventType());
        assertEquals("Home Page", event.getEventName());
        assertEquals(1234567890L, event.getTimestamp());
        assertTrue(event.isTriggerable());
        assertNotNull(event.getParameters());
    }

    @Test
    void testDeserializeEventWithAllFields() throws IOException {
        // Arrange: Create a complete event with all fields
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("event_id", "evt-789");
        eventMap.put("user_id", "user-101");
        eventMap.put("domain", "test.com");
        eventMap.put("is_first_in_visit", true);
        eventMap.put("is_last_in_visit", false);
        eventMap.put("is_first_event", true);
        eventMap.put("is_current", true);
        eventMap.put("event_name", "Button Click");
        eventMap.put("event_displayname", "Click Button");
        eventMap.put("integration", "web");
        eventMap.put("app", "myapp");
        eventMap.put("platform", "desktop");
        eventMap.put("is_https", true);
        eventMap.put("event_type", "click");
        eventMap.put("duration", 5000L);
        eventMap.put("timestamp", 9876543210L);
        eventMap.put("triggerable", false);
        
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> customParams = new HashMap<>();
        customParams.put("button_id", "submit-btn");
        params.put("custom_params", customParams);
        params.put("utm_source", "facebook");
        params.put("utm_campaign", "summer-sale");
        eventMap.put("parameters", params);
        
        String json = objectMapper.writeValueAsString(eventMap);
        byte[] message = json.getBytes();

        // Act
        UserEvent event = deserializer.deserialize(message);

        // Assert
        assertNotNull(event);
        assertEquals("evt-789", event.getEventId());
        assertEquals("user-101", event.getUserId());
        assertTrue(event.isFirstInVisit());
        assertFalse(event.isLastInVisit());
        assertEquals("click", event.getEventType());
        assertEquals(5000L, event.getDuration());
        assertNotNull(event.getParameters());
        assertEquals("facebook", event.getParameters().getUtmSource());
        assertEquals("summer-sale", event.getParameters().getUtmCampaign());
    }

    @Test
    void testDeserializeInvalidJson() throws IOException {
        // Arrange: Invalid JSON
        String invalidJson = "{invalid json}";
        byte[] message = invalidJson.getBytes();

        // Act: Deserialize should return null for invalid JSON
        UserEvent event = deserializer.deserialize(message);

        // Assert: Should return null and not throw exception
        assertNull(event);
    }

    @Test
    void testDeserializeNullMessage() throws IOException {
        // Act: Deserialize null message
        UserEvent event = deserializer.deserialize(null);

        // Assert: Should return null
        assertNull(event);
    }

    @Test
    void testDeserializeEmptyMessage() throws IOException {
        // Arrange: Empty byte array
        byte[] message = new byte[0];

        // Act
        UserEvent event = deserializer.deserialize(message);

        // Assert: Should return null
        assertNull(event);
    }

    @Test
    void testDeserializeMissingUserId() throws IOException {
        // Arrange: Event without userId
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("event_id", "evt-999");
        eventMap.put("event_type", "page_view");
        eventMap.put("timestamp", 1234567890L);
        
        String json = objectMapper.writeValueAsString(eventMap);
        byte[] message = json.getBytes();

        // Act: Should return null because userId is required
        UserEvent event = deserializer.deserialize(message);

        // Assert
        assertNull(event);
    }

    @Test
    void testDeserializeMissingEventType() throws IOException {
        // Arrange: Event without eventType
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("event_id", "evt-888");
        eventMap.put("user_id", "user-777");
        eventMap.put("timestamp", 1234567890L);
        
        String json = objectMapper.writeValueAsString(eventMap);
        byte[] message = json.getBytes();

        // Act: Should return null because eventType is required
        UserEvent event = deserializer.deserialize(message);

        // Assert
        assertNull(event);
    }

    @Test
    void testDeserializeEmptyUserId() throws IOException {
        // Arrange: Event with empty userId
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("event_id", "evt-666");
        eventMap.put("user_id", "");
        eventMap.put("event_type", "page_view");
        eventMap.put("timestamp", 1234567890L);
        
        String json = objectMapper.writeValueAsString(eventMap);
        byte[] message = json.getBytes();

        // Act: Should return null because userId is empty
        UserEvent event = deserializer.deserialize(message);

        // Assert
        assertNull(event);
    }

    @Test
    void testIsEndOfStream() {
        // Act & Assert: Should always return false (continuous stream)
        assertFalse(deserializer.isEndOfStream(new UserEvent()));
        assertFalse(deserializer.isEndOfStream(null));
    }

    @Test
    void testGetProducedType() {
        // Act & Assert: Should return UserEvent type information
        assertNotNull(deserializer.getProducedType());
        assertEquals(UserEvent.class, deserializer.getProducedType().getTypeClass());
    }

    @Test
    void testDeserializeWithoutOpenCall() throws IOException {
        // Arrange: Create a new deserializer without calling open()
        UserEventDeserializer newDeserializer = new UserEventDeserializer();
        
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("event_id", "evt-555");
        eventMap.put("user_id", "user-444");
        eventMap.put("event_type", "click");
        eventMap.put("timestamp", 1234567890L);
        
        String json = objectMapper.writeValueAsString(eventMap);
        byte[] message = json.getBytes();

        // Act: Should still work with lazy initialization
        UserEvent event = newDeserializer.deserialize(message);

        // Assert: Should deserialize successfully
        assertNotNull(event);
        assertEquals("evt-555", event.getEventId());
        assertEquals("user-444", event.getUserId());
    }

    @Test
    void testDeserializePartialEvent() throws IOException {
        // Arrange: Event with only required fields
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("user_id", "user-minimal");
        eventMap.put("event_type", "minimal_event");
        
        String json = objectMapper.writeValueAsString(eventMap);
        byte[] message = json.getBytes();

        // Act
        UserEvent event = deserializer.deserialize(message);

        // Assert: Should deserialize with only required fields
        assertNotNull(event);
        assertEquals("user-minimal", event.getUserId());
        assertEquals("minimal_event", event.getEventType());
        assertNull(event.getEventId()); // Optional field
        assertNull(event.getDomain()); // Optional field
    }
}
