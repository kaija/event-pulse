package com.example.flink.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserEvent data model
 */
class UserEventTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testUserEventSerialization() throws Exception {
        UserEvent event = new UserEvent();
        event.setEventId("evt-123");
        event.setUserId("user-456");
        event.setEventName("page_view");
        event.setEventType("pageview");
        event.setTimestamp(System.currentTimeMillis());
        event.setTriggerable(true);

        EventParameters params = new EventParameters();
        Map<String, Object> customParams = new HashMap<>();
        customParams.put("page", "/home");
        params.setCustomParams(customParams);
        params.setUtmSource("google");
        event.setParameters(params);

        String json = objectMapper.writeValueAsString(event);
        assertNotNull(json);
        assertTrue(json.contains("evt-123"));
        assertTrue(json.contains("user-456"));
    }

    @Test
    void testUserEventDeserialization() throws Exception {
        String json = "{"
                + "\"event_id\":\"evt-123\","
                + "\"user_id\":\"user-456\","
                + "\"event_name\":\"page_view\","
                + "\"event_type\":\"pageview\","
                + "\"timestamp\":1234567890,"
                + "\"triggerable\":true,"
                + "\"parameters\":{"
                + "  \"custom_params\":{\"page\":\"/home\"},"
                + "  \"utm_source\":\"google\""
                + "}"
                + "}";

        UserEvent event = objectMapper.readValue(json, UserEvent.class);
        assertNotNull(event);
        assertEquals("evt-123", event.getEventId());
        assertEquals("user-456", event.getUserId());
        assertEquals("page_view", event.getEventName());
        assertEquals("pageview", event.getEventType());
        assertEquals(1234567890, event.getTimestamp());
        assertTrue(event.isTriggerable());
        assertNotNull(event.getParameters());
        assertEquals("google", event.getParameters().getUtmSource());
    }

    @Test
    void testUserEventRequiredFields() {
        UserEvent event = new UserEvent();
        event.setEventId("evt-123");
        event.setUserId("user-456");
        event.setEventType("pageview");

        assertNotNull(event.getEventId());
        assertNotNull(event.getUserId());
        assertNotNull(event.getEventType());
    }
}
