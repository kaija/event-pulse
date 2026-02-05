package com.example.flink.filter;

import com.example.flink.model.*;
import org.apache.flink.configuration.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EventFilterFunction.
 * Tests requirements 3.1, 3.2, 3.3, 3.4, 3.5, and 3.6.
 */
class EventFilterFunctionTest {

    private EnrichedEvent createTestEvent(String userId, String eventType, String country) {
        UserEvent event = new UserEvent();
        event.setEventId("test-event-1");
        event.setUserId(userId);
        event.setEventType(eventType);
        event.setEventName("test_event");
        event.setTimestamp(System.currentTimeMillis());

        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setCountry(country);
        profile.setCity("TestCity");
        profile.setLanguage("en");

        Visit visit = new Visit();
        visit.setUuid("visit-1");
        visit.setDevice("desktop");
        visit.setBrowser("Chrome");

        List<EventHistory> history = new ArrayList<>();

        return new EnrichedEvent(event, profile, visit, history);
    }

    @Test
    void testFilterFunctionInitialization() throws Exception {
        // Test requirement 3.1: Filter function inherits RichFilterFunction
        // Test requirement 3.2: Filter function loads AviatorScript in open()
        EventFilterFunction filterFunction = new EventFilterFunction("true");
        
        assertNotNull(filterFunction);
        assertEquals("true", filterFunction.getFilterScript());
        
        // Initialize the function
        filterFunction.open(new Configuration());
        
        // Should not throw exception
    }

    @Test
    void testFilterPassesWhenScriptReturnsTrue() throws Exception {
        // Test requirement 3.4: Filter allows event when script evaluates to true
        EventFilterFunction filterFunction = new EventFilterFunction("true");
        filterFunction.open(new Configuration());

        EnrichedEvent event = createTestEvent("user1", "page_view", "US");
        
        boolean result = filterFunction.filter(event);
        
        assertTrue(result, "Filter should pass when script returns true");
    }

    @Test
    void testFilterRejectsWhenScriptReturnsFalse() throws Exception {
        // Test requirement 3.5: Filter rejects event when script evaluates to false
        EventFilterFunction filterFunction = new EventFilterFunction("false");
        filterFunction.open(new Configuration());

        EnrichedEvent event = createTestEvent("user1", "page_view", "US");
        
        boolean result = filterFunction.filter(event);
        
        assertFalse(result, "Filter should reject when script returns false");
    }

    @Test
    void testFilterWithEventCondition() throws Exception {
        // Test requirement 3.3: Filter executes AviatorScript to evaluate event
        // Filter only page_view events
        EventFilterFunction filterFunction = new EventFilterFunction(
            "event.eventType == 'page_view'"
        );
        filterFunction.open(new Configuration());

        EnrichedEvent pageViewEvent = createTestEvent("user1", "page_view", "US");
        EnrichedEvent clickEvent = createTestEvent("user1", "click", "US");
        
        assertTrue(filterFunction.filter(pageViewEvent), 
            "Should pass page_view events");
        assertFalse(filterFunction.filter(clickEvent), 
            "Should reject non-page_view events");
    }

    @Test
    void testFilterWithUserProfileCondition() throws Exception {
        // Test requirement 3.3: Filter can access user profile data
        // Filter only US users
        EventFilterFunction filterFunction = new EventFilterFunction(
            "user.country == 'US'"
        );
        filterFunction.open(new Configuration());

        EnrichedEvent usEvent = createTestEvent("user1", "page_view", "US");
        EnrichedEvent ukEvent = createTestEvent("user2", "page_view", "UK");
        
        assertTrue(filterFunction.filter(usEvent), 
            "Should pass US users");
        assertFalse(filterFunction.filter(ukEvent), 
            "Should reject non-US users");
    }

    @Test
    void testFilterWithComplexCondition() throws Exception {
        // Test complex AviatorScript expression
        EventFilterFunction filterFunction = new EventFilterFunction(
            "event.eventType == 'page_view' && user.country == 'US'"
        );
        filterFunction.open(new Configuration());

        EnrichedEvent matchingEvent = createTestEvent("user1", "page_view", "US");
        EnrichedEvent wrongType = createTestEvent("user1", "click", "US");
        EnrichedEvent wrongCountry = createTestEvent("user2", "page_view", "UK");
        
        assertTrue(filterFunction.filter(matchingEvent), 
            "Should pass events matching both conditions");
        assertFalse(filterFunction.filter(wrongType), 
            "Should reject events with wrong type");
        assertFalse(filterFunction.filter(wrongCountry), 
            "Should reject events with wrong country");
    }

    @Test
    void testFilterRejectsOnScriptError() throws Exception {
        // Test requirement 3.6: Filter rejects event when script execution fails
        EventFilterFunction filterFunction = new EventFilterFunction(
            "event.nonExistentField.someMethod()"
        );
        filterFunction.open(new Configuration());

        EnrichedEvent event = createTestEvent("user1", "page_view", "US");
        
        // Should not throw exception, but should return false (reject event)
        boolean result = filterFunction.filter(event);
        
        assertFalse(result, "Filter should reject event when script execution fails");
    }

    @Test
    void testFilterWithNullResult() throws Exception {
        // Test that null result is treated as false
        EventFilterFunction filterFunction = new EventFilterFunction("nil");
        filterFunction.open(new Configuration());

        EnrichedEvent event = createTestEvent("user1", "page_view", "US");
        
        boolean result = filterFunction.filter(event);
        
        assertFalse(result, "Filter should reject when script returns null");
    }

    @Test
    void testInvalidScriptThrowsExceptionOnOpen() {
        // Test that invalid script throws exception during initialization
        EventFilterFunction filterFunction = new EventFilterFunction(
            "this is not valid aviator script @#$%"
        );
        
        assertThrows(RuntimeException.class, () -> {
            filterFunction.open(new Configuration());
        }, "Should throw exception when script cannot be compiled");
    }

    @Test
    void testFilterCanAccessVisitData() throws Exception {
        // Test that filter can access visit information
        EventFilterFunction filterFunction = new EventFilterFunction(
            "visit.device == 'desktop'"
        );
        filterFunction.open(new Configuration());

        EnrichedEvent event = createTestEvent("user1", "page_view", "US");
        
        boolean result = filterFunction.filter(event);
        
        assertTrue(result, "Filter should be able to access visit data");
    }

    @Test
    void testFilterCanAccessEventHistory() throws Exception {
        // Test that filter can access event history
        EventFilterFunction filterFunction = new EventFilterFunction(
            "history != nil"
        );
        filterFunction.open(new Configuration());

        EnrichedEvent event = createTestEvent("user1", "page_view", "US");
        
        boolean result = filterFunction.filter(event);
        
        assertTrue(result, "Filter should be able to access event history");
    }
}
