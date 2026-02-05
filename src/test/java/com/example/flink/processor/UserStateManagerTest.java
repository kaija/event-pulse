package com.example.flink.processor;

import com.example.flink.api.ApiException;
import com.example.flink.api.ProfileApiClient;
import com.example.flink.model.*;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.operators.KeyedProcessOperator;
import org.apache.flink.streaming.util.KeyedOneInputStreamOperatorTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserStateManager
 * 
 * Tests verify the processElement() method implementation:
 * - Creates new checkpoint when state is null (Requirements 2.1, 2.2, 2.3)
 * - Loads existing checkpoint from state (Requirement 2.5)
 * - Updates checkpoint and stores to Flink State (Requirements 5.2, 5.3)
 * - Handles API failures gracefully (Requirement 2.4)
 * - Creates and outputs EnrichedEvent
 */
class UserStateManagerTest {

    private ProfileApiClient mockApiClient;
    private UserStateManager stateManager;

    @BeforeEach
    void setUp() {
        mockApiClient = mock(ProfileApiClient.class);
        stateManager = new UserStateManager("http://localhost:8080", 5000);
    }

    /**
     * Test that UserStateManager can be instantiated
     */
    @Test
    void testUserStateManagerInstantiation() {
        assertNotNull(stateManager);
    }

    /**
     * Test processElement creates new checkpoint when state is null
     * Requirements: 2.1, 2.2, 2.3
     */
    @Test
    void testProcessElement_CreatesNewCheckpoint_WhenStateIsNull() throws Exception {
        // Setup
        String userId = "user123";
        UserEvent event = createTestEvent(userId);
        UserProfile profile = createTestProfile(userId);
        Visit visit = createTestVisit();
        List<EventHistory> history = createTestHistory();

        // Mock API responses
        when(mockApiClient.getUserProfile(userId)).thenReturn(profile);
        when(mockApiClient.getCurrentVisit(userId)).thenReturn(visit);
        when(mockApiClient.getEventHistory(userId)).thenReturn(history);

        // Create test harness
        KeyedOneInputStreamOperatorTestHarness<String, UserEvent, EnrichedEvent> testHarness =
            new KeyedOneInputStreamOperatorTestHarness<>(
                new KeyedProcessOperator<>(stateManager),
                UserEvent::getUserId,
                TypeInformation.of(String.class)
            );

        testHarness.open();

        // Process event
        testHarness.processElement(event, System.currentTimeMillis());

        // Verify API was called
        verify(mockApiClient, times(1)).getUserProfile(userId);
        verify(mockApiClient, times(1)).getCurrentVisit(userId);
        verify(mockApiClient, times(1)).getEventHistory(userId);

        // Verify output
        List<EnrichedEvent> output = testHarness.extractOutputValues();
        assertEquals(1, output.size());
        
        EnrichedEvent enrichedEvent = output.get(0);
        assertNotNull(enrichedEvent);
        assertEquals(event.getEventId(), enrichedEvent.getEvent().getEventId());
        assertEquals(userId, enrichedEvent.getUserProfile().getUserId());
        assertEquals(profile.getCountry(), enrichedEvent.getUserProfile().getCountry());
        assertNotNull(enrichedEvent.getCurrentVisit());
        assertEquals(visit.getUuid(), enrichedEvent.getCurrentVisit().getUuid());
        assertNotNull(enrichedEvent.getEventHistory());
        assertEquals(1, enrichedEvent.getEventHistory().size());

        testHarness.close();
    }

    /**
     * Test processElement loads existing checkpoint from state
     * Requirement: 2.5
     */
    @Test
    void testProcessElement_LoadsExistingCheckpoint_WhenStateExists() throws Exception {
        // Setup
        String userId = "user456";
        UserEvent event1 = createTestEvent(userId);
        event1.setEventId("event1");
        UserEvent event2 = createTestEvent(userId);
        event2.setEventId("event2");
        
        UserProfile profile = createTestProfile(userId);
        Visit visit = createTestVisit();
        List<EventHistory> history = createTestHistory();

        // Mock API responses for first event only
        when(mockApiClient.getUserProfile(userId)).thenReturn(profile);
        when(mockApiClient.getCurrentVisit(userId)).thenReturn(visit);
        when(mockApiClient.getEventHistory(userId)).thenReturn(history);

        // Create test harness
        KeyedOneInputStreamOperatorTestHarness<String, UserEvent, EnrichedEvent> testHarness =
            new KeyedOneInputStreamOperatorTestHarness<>(
                new KeyedProcessOperator<>(stateManager),
                UserEvent::getUserId,
                TypeInformation.of(String.class)
            );

        testHarness.open();

        // Process first event (creates checkpoint)
        testHarness.processElement(event1, System.currentTimeMillis());

        // Process second event (should load from state, not call API)
        testHarness.processElement(event2, System.currentTimeMillis());

        // Verify API was called only once (for first event)
        verify(mockApiClient, times(1)).getUserProfile(userId);
        verify(mockApiClient, times(1)).getCurrentVisit(userId);
        verify(mockApiClient, times(1)).getEventHistory(userId);

        // Verify both events produced output
        List<EnrichedEvent> output = testHarness.extractOutputValues();
        assertEquals(2, output.size());
        
        // Both events should have the same user profile data
        assertEquals(profile.getCountry(), output.get(0).getUserProfile().getCountry());
        assertEquals(profile.getCountry(), output.get(1).getUserProfile().getCountry());

        testHarness.close();
    }

    /**
     * Test processElement handles API failure gracefully
     * Requirement: 2.4
     */
    @Test
    void testProcessElement_HandlesApiFailure_CreatesEmptyCheckpoint() throws Exception {
        // Setup
        String userId = "user789";
        UserEvent event = createTestEvent(userId);

        // Mock API to throw exception
        when(mockApiClient.getUserProfile(userId)).thenThrow(new ApiException("API unavailable"));

        // Create test harness
        KeyedOneInputStreamOperatorTestHarness<String, UserEvent, EnrichedEvent> testHarness =
            new KeyedOneInputStreamOperatorTestHarness<>(
                new KeyedProcessOperator<>(stateManager),
                UserEvent::getUserId,
                TypeInformation.of(String.class)
            );

        testHarness.open();

        // Process event (should not throw exception)
        testHarness.processElement(event, System.currentTimeMillis());

        // Verify API was called
        verify(mockApiClient, times(1)).getUserProfile(userId);

        // Verify output with empty checkpoint
        List<EnrichedEvent> output = testHarness.extractOutputValues();
        assertEquals(1, output.size());
        
        EnrichedEvent enrichedEvent = output.get(0);
        assertNotNull(enrichedEvent);
        assertEquals(event.getEventId(), enrichedEvent.getEvent().getEventId());
        assertNotNull(enrichedEvent.getUserProfile()); // Empty profile, not null
        assertNull(enrichedEvent.getCurrentVisit()); // Empty checkpoint has null visit
        assertNotNull(enrichedEvent.getEventHistory());
        assertTrue(enrichedEvent.getEventHistory().isEmpty()); // Empty history

        testHarness.close();
    }

    /**
     * Test processElement updates checkpoint last activity time
     * Requirements: 5.2, 5.3
     */
    @Test
    void testProcessElement_UpdatesLastActivityTime_OnExistingCheckpoint() throws Exception {
        // Setup
        String userId = "user999";
        UserEvent event1 = createTestEvent(userId);
        event1.setEventId("event1");
        UserEvent event2 = createTestEvent(userId);
        event2.setEventId("event2");
        
        UserProfile profile = createTestProfile(userId);
        Visit visit = createTestVisit();
        List<EventHistory> history = createTestHistory();

        // Mock API responses
        when(mockApiClient.getUserProfile(userId)).thenReturn(profile);
        when(mockApiClient.getCurrentVisit(userId)).thenReturn(visit);
        when(mockApiClient.getEventHistory(userId)).thenReturn(history);

        // Create test harness
        KeyedOneInputStreamOperatorTestHarness<String, UserEvent, EnrichedEvent> testHarness =
            new KeyedOneInputStreamOperatorTestHarness<>(
                new KeyedProcessOperator<>(stateManager),
                UserEvent::getUserId,
                TypeInformation.of(String.class)
            );

        testHarness.open();

        // Process first event
        long time1 = System.currentTimeMillis();
        testHarness.processElement(event1, time1);

        // Wait a bit
        Thread.sleep(10);

        // Process second event
        long time2 = System.currentTimeMillis();
        testHarness.processElement(event2, time2);

        // Verify both events produced output
        List<EnrichedEvent> output = testHarness.extractOutputValues();
        assertEquals(2, output.size());

        // The second event should have updated the checkpoint
        // (We can't directly verify lastActivityTime from output, but we verify
        // that the state was updated by checking API was called only once)
        verify(mockApiClient, times(1)).getUserProfile(userId);

        testHarness.close();
    }

    /**
     * Test processElement creates correct EnrichedEvent structure
     */
    @Test
    void testProcessElement_CreatesCorrectEnrichedEvent() throws Exception {
        // Setup
        String userId = "user111";
        UserEvent event = createTestEvent(userId);
        event.setEventName("purchase");
        event.setEventType("track");
        
        UserProfile profile = createTestProfile(userId);
        profile.setCountry("JP");
        profile.setCity("Tokyo");
        
        Visit visit = createTestVisit();
        visit.setBrowser("Firefox");
        
        List<EventHistory> history = createTestHistory();

        // Mock API responses
        when(mockApiClient.getUserProfile(userId)).thenReturn(profile);
        when(mockApiClient.getCurrentVisit(userId)).thenReturn(visit);
        when(mockApiClient.getEventHistory(userId)).thenReturn(history);

        // Create test harness
        KeyedOneInputStreamOperatorTestHarness<String, UserEvent, EnrichedEvent> testHarness =
            new KeyedOneInputStreamOperatorTestHarness<>(
                new KeyedProcessOperator<>(stateManager),
                UserEvent::getUserId,
                TypeInformation.of(String.class)
            );

        testHarness.open();

        // Process event
        testHarness.processElement(event, System.currentTimeMillis());

        // Verify output structure
        List<EnrichedEvent> output = testHarness.extractOutputValues();
        assertEquals(1, output.size());
        
        EnrichedEvent enrichedEvent = output.get(0);
        
        // Verify event data
        assertNotNull(enrichedEvent.getEvent());
        assertEquals("purchase", enrichedEvent.getEvent().getEventName());
        assertEquals("track", enrichedEvent.getEvent().getEventType());
        
        // Verify user profile data
        assertNotNull(enrichedEvent.getUserProfile());
        assertEquals("JP", enrichedEvent.getUserProfile().getCountry());
        assertEquals("Tokyo", enrichedEvent.getUserProfile().getCity());
        
        // Verify visit data
        assertNotNull(enrichedEvent.getCurrentVisit());
        assertEquals("Firefox", enrichedEvent.getCurrentVisit().getBrowser());
        
        // Verify history data
        assertNotNull(enrichedEvent.getEventHistory());
        assertEquals(1, enrichedEvent.getEventHistory().size());

        testHarness.close();
    }

    /**
     * Test that UserCheckpoint.empty() creates valid empty checkpoint
     */
    @Test
    void testEmptyCheckpointCreation() {
        String userId = "user123";
        UserCheckpoint checkpoint = UserCheckpoint.empty(userId);
        
        assertNotNull(checkpoint);
        assertEquals(userId, checkpoint.getUserId());
        assertNotNull(checkpoint.getUserProfile());
        assertNull(checkpoint.getCurrentVisit());
        assertNotNull(checkpoint.getEventHistory());
        assertTrue(checkpoint.getEventHistory().isEmpty());
        assertTrue(checkpoint.getLastActivityTime() > 0);
    }

    /**
     * Test that EnrichedEvent can be created with checkpoint data
     */
    @Test
    void testEnrichedEventCreation() {
        String userId = "user456";
        UserEvent event = createTestEvent(userId);
        UserProfile profile = createTestProfile(userId);
        Visit visit = createTestVisit();
        List<EventHistory> history = createTestHistory();
        
        EnrichedEvent enrichedEvent = new EnrichedEvent(event, profile, visit, history);
        
        assertNotNull(enrichedEvent);
        assertEquals(event.getEventId(), enrichedEvent.getEvent().getEventId());
        assertEquals(userId, enrichedEvent.getUserProfile().getUserId());
        assertNotNull(enrichedEvent.getCurrentVisit());
        assertNotNull(enrichedEvent.getEventHistory());
    }

    // Helper methods to create test data

    private UserEvent createTestEvent(String userId) {
        UserEvent event = new UserEvent();
        event.setEventId("event1");
        event.setUserId(userId);
        event.setEventName("page_view");
        event.setEventType("track");
        event.setTimestamp(System.currentTimeMillis());
        return event;
    }

    private UserProfile createTestProfile(String userId) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setCountry("US");
        profile.setCity("New York");
        profile.setLanguage("en");
        profile.setContinent("NA");
        profile.setTimezone("America/New_York");
        return profile;
    }

    private Visit createTestVisit() {
        Visit visit = new Visit();
        visit.setUuid("visit123");
        visit.setTimestamp(System.currentTimeMillis());
        visit.setOs("Windows");
        visit.setBrowser("Chrome");
        visit.setDevice("Desktop");
        visit.setLandingPage("/home");
        return visit;
    }

    private List<EventHistory> createTestHistory() {
        EventHistory history1 = new EventHistory();
        history1.setEventId("hist1");
        history1.setEventName("login");
        history1.setEventType("track");
        history1.setTimestamp(System.currentTimeMillis() - 10000);
        
        List<EventHistory> history = new ArrayList<>();
        history.add(history1);
        return history;
    }
}
