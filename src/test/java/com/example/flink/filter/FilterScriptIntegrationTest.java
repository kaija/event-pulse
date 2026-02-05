package com.example.flink.filter;

import com.example.flink.model.*;
import org.apache.flink.configuration.Configuration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the example filter.av script.
 * Verifies that the example script can be loaded and executed successfully.
 */
class FilterScriptIntegrationTest {

    private String loadFilterScript() throws IOException {
        return Files.readString(Paths.get("scripts/filter.av"));
    }

    private EnrichedEvent createTestEvent() {
        UserEvent event = new UserEvent();
        event.setEventId("test-event-1");
        event.setUserId("user123");
        event.setEventType("page_view");
        event.setEventName("home_page_view");
        event.setTimestamp(System.currentTimeMillis());
        event.setDuration(5000);
        event.setPlatform("web");
        event.setHttps(true);
        event.setTriggerable(true);

        EventParameters params = new EventParameters();
        params.setUtmSource("google");
        params.setUtmCampaign("summer_sale");
        event.setParameters(params);

        UserProfile profile = new UserProfile();
        profile.setUserId("user123");
        profile.setCountry("US");
        profile.setCity("New York");
        profile.setLanguage("en");
        profile.setContinent("North America");
        profile.setTimezone("America/New_York");

        Visit visit = new Visit();
        visit.setUuid("visit-1");
        visit.setDevice("mobile");
        visit.setBrowser("Chrome");
        visit.setOs("iOS");
        visit.setFirstVisit(true);
        visit.setReferrerType("search");
        visit.setActions(3);

        List<EventHistory> history = new ArrayList<>();
        EventHistory hist1 = new EventHistory();
        hist1.setEventId("hist-1");
        hist1.setEventName("previous_event");
        hist1.setEventType("click");
        hist1.setTimestamp(System.currentTimeMillis() - 10000);
        history.add(hist1);

        return new EnrichedEvent(event, profile, visit, history);
    }

    @Test
    void testDefaultFilterScriptLoadsSuccessfully() throws Exception {
        // Load the example filter.av script
        String filterScript = loadFilterScript();
        
        assertNotNull(filterScript, "Filter script should be loaded");
        assertFalse(filterScript.isEmpty(), "Filter script should not be empty");
        
        // Create filter function with the loaded script
        EventFilterFunction filterFunction = new EventFilterFunction(filterScript);
        filterFunction.open(new Configuration());
        
        // The default script returns true, so all events should pass
        EnrichedEvent event = createTestEvent();
        boolean result = filterFunction.filter(event);
        
        assertTrue(result, "Default filter script should pass all events");
    }

    @Test
    void testFilterExamplesDocumentationExists() throws IOException {
        // Verify that the FILTER_EXAMPLES.md documentation file exists
        assertTrue(Files.exists(Paths.get("scripts/FILTER_EXAMPLES.md")), 
            "FILTER_EXAMPLES.md documentation should exist");
        
        String documentation = Files.readString(Paths.get("scripts/FILTER_EXAMPLES.md"));
        
        // Verify the documentation contains information about available variables
        assertTrue(documentation.contains("event"), 
            "Documentation should describe the 'event' variable");
        assertTrue(documentation.contains("user"), 
            "Documentation should describe the 'user' variable");
        assertTrue(documentation.contains("visit"), 
            "Documentation should describe the 'visit' variable");
        assertTrue(documentation.contains("history"), 
            "Documentation should describe the 'history' variable");
        
        // Verify it contains example filters
        assertTrue(documentation.contains("Example"), 
            "Documentation should contain example filters");
    }

    @Test
    void testExampleFiltersAreSyntacticallyValid() throws Exception {
        // Test that commented example filters can be uncommented and work
        
        // Example 1: Filter by event type
        EventFilterFunction filter1 = new EventFilterFunction("event.eventType == 'page_view'");
        filter1.open(new Configuration());
        assertTrue(filter1.filter(createTestEvent()));
        
        // Example 2: Filter by user country
        EventFilterFunction filter2 = new EventFilterFunction("user.country == 'US'");
        filter2.open(new Configuration());
        assertTrue(filter2.filter(createTestEvent()));
        
        // Example 3: Filter by visit device
        EventFilterFunction filter3 = new EventFilterFunction("visit != nil && visit.device == 'mobile'");
        filter3.open(new Configuration());
        assertTrue(filter3.filter(createTestEvent()));
        
        // Example 4: Filter first-time visitors
        EventFilterFunction filter4 = new EventFilterFunction("visit != nil && visit.isFirstVisit == true");
        filter4.open(new Configuration());
        assertTrue(filter4.filter(createTestEvent()));
        
        // Example 5: Filter by UTM source
        EventFilterFunction filter5 = new EventFilterFunction(
            "event.parameters != nil && event.parameters.utmSource == 'google'"
        );
        filter5.open(new Configuration());
        assertTrue(filter5.filter(createTestEvent()));
        
        // Example 6: Filter by history count
        EventFilterFunction filter6 = new EventFilterFunction("history != nil && count(history) > 0");
        filter6.open(new Configuration());
        assertTrue(filter6.filter(createTestEvent()));
    }

    @Test
    void testComplexExampleFilter() throws Exception {
        // Test a complex filter combining multiple conditions
        EventFilterFunction filterFunction = new EventFilterFunction(
            "user.country == 'US' && " +
            "visit != nil && visit.device == 'mobile' && " +
            "event.eventType == 'page_view'"
        );
        filterFunction.open(new Configuration());
        
        EnrichedEvent event = createTestEvent();
        boolean result = filterFunction.filter(event);
        
        assertTrue(result, "Complex filter should pass for matching event");
    }
}
