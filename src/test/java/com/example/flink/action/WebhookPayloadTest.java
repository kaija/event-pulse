package com.example.flink.action;

import com.example.flink.model.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebhookPayload 單元測試
 */
class WebhookPayloadTest {
    
    @Test
    void testConstructorFromEnrichedEvent() {
        // Arrange
        UserEvent userEvent = createTestUserEvent();
        UserProfile userProfile = createTestUserProfile();
        Visit visit = createTestVisit();
        List<EventHistory> eventHistory = createTestEventHistory();
        
        EnrichedEvent enrichedEvent = new EnrichedEvent(userEvent, userProfile, visit, eventHistory);
        long triggeredAt = System.currentTimeMillis();
        
        // Act
        WebhookPayload payload = new WebhookPayload(enrichedEvent, triggeredAt);
        
        // Assert
        assertNotNull(payload.getEvent());
        assertEquals(userEvent.getEventId(), payload.getEvent().getEventId());
        assertEquals(userEvent.getUserId(), payload.getEvent().getUserId());
        
        assertNotNull(payload.getUserProfile());
        assertEquals(userProfile.getUserId(), payload.getUserProfile().getUserId());
        
        assertNotNull(payload.getCurrentVisit());
        assertEquals(visit.getUuid(), payload.getCurrentVisit().getUuid());
        
        assertNotNull(payload.getEventHistory());
        assertEquals(eventHistory.size(), payload.getEventHistory().size());
        
        assertEquals(triggeredAt, payload.getTriggeredAt());
    }
    
    @Test
    void testSettersAndGetters() {
        // Arrange
        WebhookPayload payload = new WebhookPayload();
        UserEvent userEvent = createTestUserEvent();
        UserProfile userProfile = createTestUserProfile();
        Visit visit = createTestVisit();
        List<EventHistory> eventHistory = createTestEventHistory();
        long triggeredAt = System.currentTimeMillis();
        
        // Act
        payload.setEvent(userEvent);
        payload.setUserProfile(userProfile);
        payload.setCurrentVisit(visit);
        payload.setEventHistory(eventHistory);
        payload.setTriggeredAt(triggeredAt);
        
        // Assert
        assertEquals(userEvent, payload.getEvent());
        assertEquals(userProfile, payload.getUserProfile());
        assertEquals(visit, payload.getCurrentVisit());
        assertEquals(eventHistory, payload.getEventHistory());
        assertEquals(triggeredAt, payload.getTriggeredAt());
    }
    
    @Test
    void testToString() {
        // Arrange
        UserEvent userEvent = createTestUserEvent();
        UserProfile userProfile = createTestUserProfile();
        Visit visit = createTestVisit();
        List<EventHistory> eventHistory = createTestEventHistory();
        EnrichedEvent enrichedEvent = new EnrichedEvent(userEvent, userProfile, visit, eventHistory);
        long triggeredAt = System.currentTimeMillis();
        
        WebhookPayload payload = new WebhookPayload(enrichedEvent, triggeredAt);
        
        // Act
        String result = payload.toString();
        
        // Assert
        assertTrue(result.contains("WebhookPayload"));
        assertTrue(result.contains("event="));
        assertTrue(result.contains("userProfile="));
    }
    
    @Test
    void testEmptyConstructor() {
        // Act
        WebhookPayload payload = new WebhookPayload();
        
        // Assert
        assertNull(payload.getEvent());
        assertNull(payload.getUserProfile());
        assertNull(payload.getCurrentVisit());
        assertNull(payload.getEventHistory());
        assertEquals(0, payload.getTriggeredAt());
    }
    
    // Helper methods to create test data
    
    private UserEvent createTestUserEvent() {
        UserEvent event = new UserEvent();
        event.setEventId("event-123");
        event.setUserId("user-456");
        event.setEventName("page_view");
        event.setEventType("pageview");
        event.setTimestamp(System.currentTimeMillis());
        event.setDomain("example.com");
        event.setParameters(new EventParameters());
        return event;
    }
    
    private UserProfile createTestUserProfile() {
        UserProfile profile = new UserProfile();
        profile.setUserId("user-456");
        profile.setCountry("US");
        profile.setCity("New York");
        profile.setLanguage("en");
        profile.setContinent("NA");
        profile.setTimezone("America/New_York");
        return profile;
    }
    
    private Visit createTestVisit() {
        Visit visit = new Visit();
        visit.setUuid("visit-789");
        visit.setTimestamp(System.currentTimeMillis());
        visit.setOs("Windows");
        visit.setBrowser("Chrome");
        visit.setDevice("Desktop");
        visit.setLandingPage("/home");
        return visit;
    }
    
    private List<EventHistory> createTestEventHistory() {
        List<EventHistory> history = new ArrayList<>();
        EventHistory event1 = new EventHistory();
        event1.setEventId("event-001");
        event1.setEventName("login");
        event1.setEventType("action");
        event1.setTimestamp(System.currentTimeMillis() - 10000);
        history.add(event1);
        return history;
    }
}
