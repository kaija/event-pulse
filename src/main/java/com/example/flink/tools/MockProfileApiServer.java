package com.example.flink.tools;

import com.example.flink.model.EventHistory;
import com.example.flink.model.EventParameters;
import com.example.flink.model.UserProfile;
import com.example.flink.model.Visit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Mock Profile API Server for testing purposes
 * 
 * This simple HTTP server simulates the Profile API and returns sample user data,
 * visit information, and event history.
 * 
 * Endpoints:
 * - GET /users/{userId}/profile - Returns user profile data
 * - GET /users/{userId}/visit - Returns current visit information
 * - GET /users/{userId}/history - Returns event history
 * 
 * Requirements: 2.2
 */
public class MockProfileApiServer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int DEFAULT_PORT = 8080;
    
    private final HttpServer server;
    private final int port;
    
    public MockProfileApiServer(int port) throws IOException {
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        setupEndpoints();
    }
    
    private void setupEndpoints() {
        server.createContext("/users/", new ProfileHandler());
        server.setExecutor(null); // Use default executor
    }
    
    public void start() {
        server.start();
        System.out.println("Mock Profile API Server started on port " + port);
        System.out.println("Available endpoints:");
        System.out.println("  GET http://localhost:" + port + "/users/{userId}/profile");
        System.out.println("  GET http://localhost:" + port + "/users/{userId}/visit");
        System.out.println("  GET http://localhost:" + port + "/users/{userId}/history");
    }
    
    public void stop() {
        server.stop(0);
        System.out.println("Mock Profile API Server stopped");
    }
    
    /**
     * Handler for all profile-related endpoints
     */
    private static class ProfileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            
            if (!"GET".equals(method)) {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                return;
            }
            
            try {
                // Parse path: /users/{userId}/{endpoint}
                String[] parts = path.split("/");
                if (parts.length < 4) {
                    sendResponse(exchange, 400, "{\"error\": \"Invalid path format\"}");
                    return;
                }
                
                String userId = parts[2];
                String endpoint = parts[3];
                
                Object responseData;
                switch (endpoint) {
                    case "profile":
                        responseData = generateUserProfile(userId);
                        break;
                    case "visit":
                        responseData = generateVisit(userId);
                        break;
                    case "history":
                        responseData = generateEventHistory(userId);
                        break;
                    default:
                        sendResponse(exchange, 404, "{\"error\": \"Endpoint not found\"}");
                        return;
                }
                
                String jsonResponse = objectMapper.writeValueAsString(responseData);
                sendResponse(exchange, 200, jsonResponse);
                
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
            }
        }
        
        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
        
        /**
         * Generate sample user profile data
         */
        private UserProfile generateUserProfile(String userId) {
            UserProfile profile = new UserProfile();
            profile.setUserId(userId);
            
            // Generate different data based on userId hash for variety
            int hash = userId.hashCode();
            
            String[] countries = {"US", "UK", "JP", "DE", "FR", "CA", "AU", "TW", "SG", "KR"};
            String[] cities = {"New York", "London", "Tokyo", "Berlin", "Paris", "Toronto", "Sydney", "Taipei", "Singapore", "Seoul"};
            String[] languages = {"en", "en-GB", "ja", "de", "fr", "en-CA", "en-AU", "zh-TW", "en-SG", "ko"};
            String[] continents = {"NA", "EU", "AS", "EU", "EU", "NA", "OC", "AS", "AS", "AS"};
            String[] timezones = {"America/New_York", "Europe/London", "Asia/Tokyo", "Europe/Berlin", "Europe/Paris", 
                                 "America/Toronto", "Australia/Sydney", "Asia/Taipei", "Asia/Singapore", "Asia/Seoul"};
            
            int index = Math.abs(hash % countries.length);
            
            profile.setCountry(countries[index]);
            profile.setCity(cities[index]);
            profile.setLanguage(languages[index]);
            profile.setContinent(continents[index]);
            profile.setTimezone(timezones[index]);
            
            return profile;
        }
        
        /**
         * Generate sample visit information
         */
        private Visit generateVisit(String userId) {
            Visit visit = new Visit();
            
            int hash = userId.hashCode();
            
            visit.setUuid(UUID.randomUUID().toString());
            visit.setTimestamp(System.currentTimeMillis() - (Math.abs(hash % 3600000))); // Within last hour
            
            String[] oses = {"Windows 10", "macOS", "Linux", "iOS", "Android"};
            String[] browsers = {"Chrome", "Firefox", "Safari", "Edge", "Opera"};
            String[] devices = {"Desktop", "Mobile", "Tablet"};
            String[] referrerTypes = {"search", "direct", "social", "email", "referral"};
            String[] screens = {"1920x1080", "1366x768", "375x667", "414x896", "768x1024"};
            
            int index = Math.abs(hash % oses.length);
            
            visit.setOs(oses[index % oses.length]);
            visit.setBrowser(browsers[index % browsers.length]);
            visit.setDevice(devices[index % devices.length]);
            visit.setLandingPage("/home");
            visit.setReferrerType(referrerTypes[index % referrerTypes.length]);
            visit.setReferrerUrl("https://www.google.com");
            visit.setReferrerQuery("search query");
            visit.setDuration(Math.abs(hash % 600000)); // Up to 10 minutes
            visit.setActions(Math.abs(hash % 20) + 1);
            visit.setFirstVisit(hash % 3 == 0); // 33% chance of first visit
            visit.setScreen(screens[index % screens.length]);
            visit.setIp("192.168." + (Math.abs(hash % 256)) + "." + (Math.abs((hash >> 8) % 256)));
            
            return visit;
        }
        
        /**
         * Generate sample event history
         */
        private List<EventHistory> generateEventHistory(String userId) {
            List<EventHistory> history = new ArrayList<>();
            
            int hash = userId.hashCode();
            int eventCount = Math.abs(hash % 5) + 1; // 1-5 events
            
            String[] eventNames = {"page_view", "button_click", "form_submit", "video_play", "purchase"};
            String[] eventTypes = {"pageview", "interaction", "conversion", "engagement", "transaction"};
            
            long baseTimestamp = System.currentTimeMillis();
            
            for (int i = 0; i < eventCount; i++) {
                EventHistory event = new EventHistory();
                event.setEventId(UUID.randomUUID().toString());
                
                int eventIndex = (Math.abs(hash) + i) % eventNames.length;
                event.setEventName(eventNames[eventIndex]);
                event.setEventType(eventTypes[eventIndex]);
                event.setTimestamp(baseTimestamp - (i * 60000)); // Each event 1 minute apart
                
                // Create sample parameters
                EventParameters params = new EventParameters();
                Map<String, Object> customParams = new HashMap<>();
                customParams.put("page", "/page" + i);
                customParams.put("value", (i + 1) * 10);
                params.setCustomParams(customParams);
                
                if (i == 0) { // Add UTM params to first event
                    params.setUtmSource("google");
                    params.setUtmCampaign("summer_sale");
                    params.setUtmMedium("cpc");
                    params.setUtmTerm("shoes");
                    params.setUtmId("campaign123");
                }
                
                event.setParameters(params);
                history.add(event);
            }
            
            return history;
        }
    }
    
    /**
     * Main method to start the mock server
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        // Allow port to be specified as command line argument
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + args[0]);
                System.err.println("Usage: java MockProfileApiServer [port]");
                System.exit(1);
            }
        }
        
        try {
            MockProfileApiServer server = new MockProfileApiServer(port);
            server.start();
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down Mock Profile API Server...");
                server.stop();
            }));
            
            System.out.println("\nPress Ctrl+C to stop the server");
            
            // Keep the server running
            Thread.currentThread().join();
            
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Server interrupted");
        }
    }
}
