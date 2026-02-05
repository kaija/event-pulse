package com.example.flink.action;

import com.example.flink.model.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ActionHandler 單元測試
 */
class ActionHandlerTest {
    
    @Test
    void testActionHandlerCanBeExtended() {
        // Arrange
        TestActionHandler handler = new TestActionHandler();
        EnrichedEvent event = createTestEnrichedEvent();
        
        // Act
        handler.execute(event);
        
        // Assert
        assertTrue(handler.wasExecuted());
        assertEquals(event, handler.getLastEvent());
    }
    
    @Test
    void testMultipleExecutions() {
        // Arrange
        TestActionHandler handler = new TestActionHandler();
        EnrichedEvent event1 = createTestEnrichedEvent();
        EnrichedEvent event2 = createTestEnrichedEvent();
        
        // Act
        handler.execute(event1);
        handler.execute(event2);
        
        // Assert
        assertTrue(handler.wasExecuted());
        assertEquals(2, handler.getExecutionCount());
        assertEquals(event2, handler.getLastEvent());
    }
    
    // Test implementation of ActionHandler
    private static class TestActionHandler extends ActionHandler {
        private boolean executed = false;
        private EnrichedEvent lastEvent = null;
        private int executionCount = 0;
        
        @Override
        public void execute(EnrichedEvent event) {
            this.executed = true;
            this.lastEvent = event;
            this.executionCount++;
        }
        
        public boolean wasExecuted() {
            return executed;
        }
        
        public EnrichedEvent getLastEvent() {
            return lastEvent;
        }
        
        public int getExecutionCount() {
            return executionCount;
        }
    }
    
    // Helper method to create test data
    private EnrichedEvent createTestEnrichedEvent() {
        UserEvent userEvent = new UserEvent();
        userEvent.setEventId("event-123");
        userEvent.setUserId("user-456");
        userEvent.setEventName("page_view");
        userEvent.setEventType("pageview");
        userEvent.setTimestamp(System.currentTimeMillis());
        
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId("user-456");
        userProfile.setCountry("US");
        
        Visit visit = new Visit();
        visit.setUuid("visit-789");
        
        List<EventHistory> eventHistory = new ArrayList<>();
        
        return new EnrichedEvent(userEvent, userProfile, visit, eventHistory);
    }
}
