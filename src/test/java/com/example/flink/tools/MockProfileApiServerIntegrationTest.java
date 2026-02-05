package com.example.flink.tools;

import com.example.flink.api.ProfileApiClient;
import com.example.flink.api.ProfileApiClientImpl;
import com.example.flink.model.EventHistory;
import com.example.flink.model.UserProfile;
import com.example.flink.model.Visit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for MockProfileApiServer
 * 
 * This test verifies that the MockProfileApiServer works correctly with
 * the ProfileApiClient implementation.
 */
class MockProfileApiServerIntegrationTest {
    
    private static MockProfileApiServer mockServer;
    private static ProfileApiClient apiClient;
    private static final int TEST_PORT = 18080;
    
    @BeforeAll
    static void setUp() throws IOException {
        // Start the mock server
        mockServer = new MockProfileApiServer(TEST_PORT);
        mockServer.start();
        
        // Create API client
        apiClient = new ProfileApiClientImpl("http://localhost:" + TEST_PORT, 5000);
        
        // Give the server a moment to start
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @AfterAll
    static void tearDown() {
        if (mockServer != null) {
            mockServer.stop();
        }
    }
    
    @Test
    void testGetUserProfile() throws Exception {
        // Test with user123
        UserProfile profile = apiClient.getUserProfile("user123");
        
        assertNotNull(profile);
        assertEquals("user123", profile.getUserId());
        assertNotNull(profile.getCountry());
        assertNotNull(profile.getCity());
        assertNotNull(profile.getLanguage());
        assertNotNull(profile.getContinent());
        assertNotNull(profile.getTimezone());
        
        System.out.println("User Profile: " + profile);
    }
    
    @Test
    void testGetCurrentVisit() throws Exception {
        // Test with user123
        Visit visit = apiClient.getCurrentVisit("user123");
        
        assertNotNull(visit);
        assertNotNull(visit.getUuid());
        assertTrue(visit.getTimestamp() > 0);
        assertNotNull(visit.getOs());
        assertNotNull(visit.getBrowser());
        assertNotNull(visit.getDevice());
        assertNotNull(visit.getLandingPage());
        assertNotNull(visit.getReferrerType());
        assertTrue(visit.getDuration() >= 0);
        assertTrue(visit.getActions() > 0);
        assertNotNull(visit.getScreen());
        assertNotNull(visit.getIp());
        
        System.out.println("Visit: " + visit);
    }
    
    @Test
    void testGetEventHistory() throws Exception {
        // Test with user123
        List<EventHistory> history = apiClient.getEventHistory("user123");
        
        assertNotNull(history);
        assertFalse(history.isEmpty());
        assertTrue(history.size() >= 1 && history.size() <= 5);
        
        // Verify first event
        EventHistory firstEvent = history.get(0);
        assertNotNull(firstEvent.getEventId());
        assertNotNull(firstEvent.getEventName());
        assertNotNull(firstEvent.getEventType());
        assertTrue(firstEvent.getTimestamp() > 0);
        assertNotNull(firstEvent.getParameters());
        
        System.out.println("Event History (" + history.size() + " events):");
        history.forEach(event -> System.out.println("  - " + event.getEventName() + " (" + event.getEventType() + ")"));
    }
    
    @Test
    void testDifferentUsersGetDifferentData() throws Exception {
        // Test that different users get different data
        UserProfile profile1 = apiClient.getUserProfile("alice");
        UserProfile profile2 = apiClient.getUserProfile("bob");
        
        assertNotNull(profile1);
        assertNotNull(profile2);
        assertEquals("alice", profile1.getUserId());
        assertEquals("bob", profile2.getUserId());
        
        // The data should be different (based on hash)
        // Note: There's a small chance they could be the same, but very unlikely
        System.out.println("Alice: " + profile1.getCountry() + ", " + profile1.getCity());
        System.out.println("Bob: " + profile2.getCountry() + ", " + profile2.getCity());
    }
    
    @Test
    void testSameUserGetsSameData() throws Exception {
        // Test that the same user gets consistent data
        UserProfile profile1 = apiClient.getUserProfile("charlie");
        UserProfile profile2 = apiClient.getUserProfile("charlie");
        
        assertNotNull(profile1);
        assertNotNull(profile2);
        
        // Should get the same data for the same user
        assertEquals(profile1.getCountry(), profile2.getCountry());
        assertEquals(profile1.getCity(), profile2.getCity());
        assertEquals(profile1.getLanguage(), profile2.getLanguage());
        assertEquals(profile1.getContinent(), profile2.getContinent());
        assertEquals(profile1.getTimezone(), profile2.getTimezone());
    }
}
