package com.example.flink.api;

import com.example.flink.model.EventHistory;
import com.example.flink.model.UserProfile;
import com.example.flink.model.Visit;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 單元測試：ProfileApiClientImpl
 * 測試 HTTP 請求、錯誤處理和逾時處理
 */
class ProfileApiClientImplTest {
    
    private MockHttpServer mockServer;
    private ProfileApiClientImpl client;
    private ObjectMapper objectMapper;
    private int serverPort;
    
    @BeforeEach
    void setUp() throws IOException {
        objectMapper = new ObjectMapper();
        
        // 找一個可用的端口
        try (ServerSocket socket = new ServerSocket(0)) {
            serverPort = socket.getLocalPort();
        }
        
        // 啟動 Mock HTTP Server
        mockServer = new MockHttpServer(serverPort);
        mockServer.start();
        
        // 建立客戶端
        String baseUrl = "http://localhost:" + serverPort;
        client = new ProfileApiClientImpl(baseUrl, 5000);
    }
    
    @AfterEach
    void tearDown() {
        if (mockServer != null) {
            mockServer.stop();
        }
    }
    
    /**
     * 測試成功取得使用者資料
     */
    @Test
    void testGetUserProfileSuccess() throws Exception {
        // 準備測試資料
        UserProfile expectedProfile = new UserProfile();
        expectedProfile.setUserId("user123");
        expectedProfile.setCountry("Taiwan");
        expectedProfile.setCity("Taipei");
        expectedProfile.setLanguage("zh-TW");
        expectedProfile.setContinent("Asia");
        expectedProfile.setTimezone("Asia/Taipei");
        
        String jsonResponse = objectMapper.writeValueAsString(expectedProfile);
        mockServer.setResponse("/users/user123/profile", 200, jsonResponse);
        
        // 執行測試
        UserProfile actualProfile = client.getUserProfile("user123");
        
        // 驗證結果
        assertNotNull(actualProfile);
        assertEquals("user123", actualProfile.getUserId());
        assertEquals("Taiwan", actualProfile.getCountry());
        assertEquals("Taipei", actualProfile.getCity());
        assertEquals("zh-TW", actualProfile.getLanguage());
        assertEquals("Asia", actualProfile.getContinent());
        assertEquals("Asia/Taipei", actualProfile.getTimezone());
    }
    
    /**
     * 測試成功取得當前訪問資訊
     */
    @Test
    void testGetCurrentVisitSuccess() throws Exception {
        // 準備測試資料
        Visit expectedVisit = new Visit();
        expectedVisit.setUuid("visit-uuid-123");
        expectedVisit.setTimestamp(System.currentTimeMillis());
        expectedVisit.setOs("Windows");
        expectedVisit.setBrowser("Chrome");
        expectedVisit.setDevice("Desktop");
        expectedVisit.setLandingPage("/home");
        expectedVisit.setReferrerType("search");
        expectedVisit.setReferrerUrl("https://google.com");
        expectedVisit.setDuration(3600000);
        expectedVisit.setActions(10);
        expectedVisit.setFirstVisit(true);
        expectedVisit.setScreen("1920x1080");
        expectedVisit.setIp("192.168.1.1");
        
        String jsonResponse = objectMapper.writeValueAsString(expectedVisit);
        mockServer.setResponse("/users/user123/visit", 200, jsonResponse);
        
        // 執行測試
        Visit actualVisit = client.getCurrentVisit("user123");
        
        // 驗證結果
        assertNotNull(actualVisit);
        assertEquals("visit-uuid-123", actualVisit.getUuid());
        assertEquals("Windows", actualVisit.getOs());
        assertEquals("Chrome", actualVisit.getBrowser());
        assertEquals("Desktop", actualVisit.getDevice());
        assertEquals("/home", actualVisit.getLandingPage());
        assertEquals("search", actualVisit.getReferrerType());
        assertEquals("https://google.com", actualVisit.getReferrerUrl());
        assertEquals(3600000, actualVisit.getDuration());
        assertEquals(10, actualVisit.getActions());
        assertTrue(actualVisit.isFirstVisit());
        assertEquals("1920x1080", actualVisit.getScreen());
        assertEquals("192.168.1.1", actualVisit.getIp());
    }
    
    /**
     * 測試成功取得事件歷史
     */
    @Test
    void testGetEventHistorySuccess() throws Exception {
        // 準備測試資料
        List<EventHistory> expectedHistory = new ArrayList<>();
        
        EventHistory event1 = new EventHistory();
        event1.setEventId("event1");
        event1.setEventName("page_view");
        event1.setEventType("tracking");
        event1.setTimestamp(System.currentTimeMillis() - 10000);
        expectedHistory.add(event1);
        
        EventHistory event2 = new EventHistory();
        event2.setEventId("event2");
        event2.setEventName("button_click");
        event2.setEventType("interaction");
        event2.setTimestamp(System.currentTimeMillis());
        expectedHistory.add(event2);
        
        String jsonResponse = objectMapper.writeValueAsString(expectedHistory);
        mockServer.setResponse("/users/user123/history", 200, jsonResponse);
        
        // 執行測試
        List<EventHistory> actualHistory = client.getEventHistory("user123");
        
        // 驗證結果
        assertNotNull(actualHistory);
        assertEquals(2, actualHistory.size());
        assertEquals("event1", actualHistory.get(0).getEventId());
        assertEquals("page_view", actualHistory.get(0).getEventName());
        assertEquals("tracking", actualHistory.get(0).getEventType());
        assertEquals("event2", actualHistory.get(1).getEventId());
        assertEquals("button_click", actualHistory.get(1).getEventName());
        assertEquals("interaction", actualHistory.get(1).getEventType());
    }
    
    /**
     * 測試 API 返回非 200 狀態碼時的錯誤處理
     */
    @Test
    void testGetUserProfileApiError() {
        mockServer.setResponse("/users/user123/profile", 500, "Internal Server Error");
        
        ApiException exception = assertThrows(ApiException.class, () -> {
            client.getUserProfile("user123");
        });
        
        assertTrue(exception.getMessage().contains("API returned status 500"));
    }
    
    /**
     * 測試 API 返回 404 時的錯誤處理
     */
    @Test
    void testGetCurrentVisitNotFound() {
        mockServer.setResponse("/users/user123/visit", 404, "Not Found");
        
        ApiException exception = assertThrows(ApiException.class, () -> {
            client.getCurrentVisit("user123");
        });
        
        assertTrue(exception.getMessage().contains("API returned status 404"));
    }
    
    /**
     * 測試無效的 JSON 回應
     */
    @Test
    void testGetUserProfileInvalidJson() {
        mockServer.setResponse("/users/user123/profile", 200, "invalid json {{{");
        
        ApiException exception = assertThrows(ApiException.class, () -> {
            client.getUserProfile("user123");
        });
        
        assertTrue(exception.getMessage().contains("Failed to fetch user profile"));
        assertNotNull(exception.getCause());
    }
    
    /**
     * 測試空的或 null 的 userId
     */
    @Test
    void testGetUserProfileWithNullUserId() {
        ApiException exception = assertThrows(ApiException.class, () -> {
            client.getUserProfile(null);
        });
        
        assertEquals("User ID cannot be null or empty", exception.getMessage());
    }
    
    /**
     * 測試空的 userId
     */
    @Test
    void testGetCurrentVisitWithEmptyUserId() {
        ApiException exception = assertThrows(ApiException.class, () -> {
            client.getCurrentVisit("");
        });
        
        assertEquals("User ID cannot be null or empty", exception.getMessage());
    }
    
    /**
     * 測試空的 userId
     */
    @Test
    void testGetEventHistoryWithEmptyUserId() {
        ApiException exception = assertThrows(ApiException.class, () -> {
            client.getEventHistory("");
        });
        
        assertEquals("User ID cannot be null or empty", exception.getMessage());
    }
    
    /**
     * 測試連接失敗（伺服器未啟動）
     */
    @Test
    void testConnectionFailure() {
        // 停止伺服器以模擬連接失敗
        mockServer.stop();
        
        ApiException exception = assertThrows(ApiException.class, () -> {
            client.getUserProfile("user123");
        });
        
        assertTrue(exception.getMessage().contains("Failed to fetch user profile"));
        assertNotNull(exception.getCause());
    }
    
    /**
     * 測試逾時處理
     */
    @Test
    void testTimeout() {
        // 建立一個逾時時間很短的客戶端
        ProfileApiClientImpl shortTimeoutClient = new ProfileApiClientImpl(
            "http://localhost:" + serverPort, 50);
        
        // 設定伺服器延遲回應（延遲時間要大於逾時時間）
        mockServer.setResponse("/users/user123/profile", 200, "{}");
        mockServer.setDelay("/users/user123/profile", 1000);
        
        ApiException exception = assertThrows(ApiException.class, () -> {
            shortTimeoutClient.getUserProfile("user123");
        });
        
        assertTrue(exception.getMessage().contains("Failed to fetch user profile"));
    }
    
    /**
     * 簡單的 Mock HTTP Server
     * 用於測試 HTTP 請求
     */
    private static class MockHttpServer {
        private final int port;
        private final ExecutorService executor;
        private com.sun.net.httpserver.HttpServer server;
        private final java.util.Map<String, ResponseConfig> responses = new java.util.HashMap<>();
        
        public MockHttpServer(int port) {
            this.port = port;
            this.executor = Executors.newSingleThreadExecutor();
        }
        
        public void start() {
            try {
                server = com.sun.net.httpserver.HttpServer.create(
                    new java.net.InetSocketAddress(port), 0);
                
                server.createContext("/", exchange -> {
                    String path = exchange.getRequestURI().getPath();
                    ResponseConfig config = responses.get(path);
                    
                    if (config != null) {
                        // 如果設定了延遲，則等待
                        if (config.delayMs > 0) {
                            try {
                                Thread.sleep(config.delayMs);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        
                        byte[] responseBytes = config.body.getBytes();
                        exchange.sendResponseHeaders(config.statusCode, responseBytes.length);
                        exchange.getResponseBody().write(responseBytes);
                    } else {
                        exchange.sendResponseHeaders(404, 0);
                    }
                    exchange.close();
                });
                
                server.setExecutor(executor);
                server.start();
            } catch (IOException e) {
                throw new RuntimeException("Failed to start mock server", e);
            }
        }
        
        public void stop() {
            if (server != null) {
                server.stop(0);
            }
            executor.shutdown();
        }
        
        public void setResponse(String path, int statusCode, String body) {
            responses.put(path, new ResponseConfig(statusCode, body, 0));
        }
        
        public void setDelay(String path, int delayMs) {
            ResponseConfig config = responses.get(path);
            if (config != null) {
                responses.put(path, new ResponseConfig(config.statusCode, config.body, delayMs));
            }
        }
        
        private static class ResponseConfig {
            final int statusCode;
            final String body;
            final int delayMs;
            
            ResponseConfig(int statusCode, String body, int delayMs) {
                this.statusCode = statusCode;
                this.body = body;
                this.delayMs = delayMs;
            }
        }
    }
}
