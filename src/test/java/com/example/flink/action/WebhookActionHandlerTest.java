package com.example.flink.action;

import com.example.flink.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebhookActionHandler 單元測試
 * 
 * 測試 Webhook 動作處理器的功能，包括成功發送、錯誤處理和逾時處理
 */
class WebhookActionHandlerTest {
    
    private HttpServer mockServer;
    private int serverPort;
    private String webhookUrl;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() throws IOException {
        // 建立 Mock HTTP 伺服器
        serverPort = 8888;
        mockServer = HttpServer.create(new InetSocketAddress(serverPort), 0);
        mockServer.start();
        webhookUrl = "http://localhost:" + serverPort + "/webhook";
        objectMapper = new ObjectMapper();
    }
    
    @AfterEach
    void tearDown() {
        if (mockServer != null) {
            mockServer.stop(0);
        }
    }
    
    /**
     * 測試成功發送 Webhook
     * 
     * 需求 4.3: 發送 HTTP POST 請求到指定的 URL
     * 需求 4.4: 記錄成功狀態
     * 需求 4.7: 在請求內容中包含事件資料和使用者資料
     */
    @Test
    void testSuccessfulWebhookSend() throws Exception {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> receivedBody = new AtomicReference<>();
        AtomicReference<String> receivedContentType = new AtomicReference<>();
        
        mockServer.createContext("/webhook", exchange -> {
            try {
                // 記錄收到的請求
                receivedContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
                byte[] requestBody = exchange.getRequestBody().readAllBytes();
                receivedBody.set(new String(requestBody));
                
                // 返回 200 OK
                String response = "{\"status\":\"success\"}";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } finally {
                latch.countDown();
            }
        });
        
        WebhookActionHandler handler = new WebhookActionHandler(webhookUrl);
        EnrichedEvent event = createTestEnrichedEvent();
        
        // Act
        handler.execute(event);
        
        // Assert
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Webhook request should be received");
        
        // 驗證 Content-Type
        assertEquals("application/json", receivedContentType.get());
        
        // 驗證請求內容包含事件資料和使用者資料（需求 4.7）
        String body = receivedBody.get();
        assertNotNull(body);
        assertTrue(body.contains("event-123")); // eventId
        assertTrue(body.contains("user-456")); // userId
        assertTrue(body.contains("US")); // country from userProfile
        assertTrue(body.contains("visit-789")); // visit uuid
        
        // 驗證可以反序列化為 WebhookPayload
        WebhookPayload payload = objectMapper.readValue(body, WebhookPayload.class);
        assertNotNull(payload.getEvent());
        assertNotNull(payload.getUserProfile());
        assertNotNull(payload.getCurrentVisit());
        assertNotNull(payload.getEventHistory());
        assertTrue(payload.getTriggeredAt() > 0);
    }
    
    /**
     * 測試 Webhook 失敗處理（HTTP 錯誤狀態碼）
     * 
     * 需求 4.5: 記錄錯誤並繼續處理
     */
    @Test
    void testWebhookFailureWithErrorStatus() throws Exception {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        
        mockServer.createContext("/webhook", exchange -> {
            try {
                // 返回 500 Internal Server Error
                String response = "{\"error\":\"Internal server error\"}";
                exchange.sendResponseHeaders(500, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } finally {
                latch.countDown();
            }
        });
        
        WebhookActionHandler handler = new WebhookActionHandler(webhookUrl);
        EnrichedEvent event = createTestEnrichedEvent();
        
        // Act - 應該不拋出例外，只記錄錯誤
        assertDoesNotThrow(() -> handler.execute(event));
        
        // Assert
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Webhook request should be received");
    }
    
    /**
     * 測試 Webhook 失敗處理（404 Not Found）
     * 
     * 需求 4.5: 記錄錯誤並繼續處理
     */
    @Test
    void testWebhookFailureWithNotFound() throws Exception {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        
        mockServer.createContext("/webhook", exchange -> {
            try {
                // 返回 404 Not Found
                String response = "{\"error\":\"Not found\"}";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } finally {
                latch.countDown();
            }
        });
        
        WebhookActionHandler handler = new WebhookActionHandler(webhookUrl);
        EnrichedEvent event = createTestEnrichedEvent();
        
        // Act - 應該不拋出例外，只記錄錯誤
        assertDoesNotThrow(() -> handler.execute(event));
        
        // Assert
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Webhook request should be received");
    }
    
    /**
     * 測試 Webhook 連接失敗處理
     * 
     * 需求 4.5: 記錄錯誤並繼續處理
     */
    @Test
    void testWebhookConnectionFailure() {
        // Arrange - 使用不存在的伺服器
        String invalidUrl = "http://localhost:9999/webhook";
        WebhookActionHandler handler = new WebhookActionHandler(invalidUrl, 1000);
        EnrichedEvent event = createTestEnrichedEvent();
        
        // Act - 應該不拋出例外，只記錄錯誤
        assertDoesNotThrow(() -> handler.execute(event));
    }
    
    /**
     * 測試 Webhook 逾時處理
     * 
     * 需求 4.5: 記錄錯誤並繼續處理
     */
    @Test
    void testWebhookTimeout() throws Exception {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        
        mockServer.createContext("/webhook", exchange -> {
            try {
                // 延遲回應以觸發逾時
                Thread.sleep(3000);
                String response = "{\"status\":\"success\"}";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });
        
        // 設定短逾時時間
        WebhookActionHandler handler = new WebhookActionHandler(webhookUrl, 500);
        EnrichedEvent event = createTestEnrichedEvent();
        
        // Act - 應該不拋出例外，只記錄錯誤
        assertDoesNotThrow(() -> handler.execute(event));
    }
    
    /**
     * 測試建構子驗證
     */
    @Test
    void testConstructorValidation() {
        // Act & Assert - null URL
        assertThrows(IllegalArgumentException.class, 
            () -> new WebhookActionHandler(null));
        
        // Act & Assert - empty URL
        assertThrows(IllegalArgumentException.class, 
            () -> new WebhookActionHandler(""));
        
        // Act & Assert - whitespace URL
        assertThrows(IllegalArgumentException.class, 
            () -> new WebhookActionHandler("   "));
    }
    
    /**
     * 測試 Getter 方法
     */
    @Test
    void testGetters() {
        // Arrange
        String url = "http://example.com/webhook";
        int timeout = 5000;
        
        // Act
        WebhookActionHandler handler = new WebhookActionHandler(url, timeout);
        
        // Assert
        assertEquals(url, handler.getWebhookUrl());
        assertEquals(timeout, handler.getTimeoutMs());
    }
    
    /**
     * 測試預設逾時時間
     */
    @Test
    void testDefaultTimeout() {
        // Arrange & Act
        WebhookActionHandler handler = new WebhookActionHandler(webhookUrl);
        
        // Assert
        assertEquals(3000, handler.getTimeoutMs());
    }
    
    /**
     * 測試多次執行
     */
    @Test
    void testMultipleExecutions() throws Exception {
        // Arrange
        AtomicInteger requestCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(3);
        
        mockServer.createContext("/webhook", exchange -> {
            try {
                requestCount.incrementAndGet();
                String response = "{\"status\":\"success\"}";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } finally {
                latch.countDown();
            }
        });
        
        WebhookActionHandler handler = new WebhookActionHandler(webhookUrl);
        EnrichedEvent event = createTestEnrichedEvent();
        
        // Act
        handler.execute(event);
        handler.execute(event);
        handler.execute(event);
        
        // Assert
        assertTrue(latch.await(5, TimeUnit.SECONDS), "All webhook requests should be received");
        assertEquals(3, requestCount.get());
    }
    
    // Helper method to create test data
    private EnrichedEvent createTestEnrichedEvent() {
        UserEvent userEvent = new UserEvent();
        userEvent.setEventId("event-123");
        userEvent.setUserId("user-456");
        userEvent.setEventName("page_view");
        userEvent.setEventType("pageview");
        userEvent.setTimestamp(System.currentTimeMillis());
        userEvent.setDomain("example.com");
        userEvent.setParameters(new EventParameters());
        
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId("user-456");
        userProfile.setCountry("US");
        userProfile.setCity("New York");
        userProfile.setLanguage("en");
        userProfile.setContinent("NA");
        userProfile.setTimezone("America/New_York");
        
        Visit visit = new Visit();
        visit.setUuid("visit-789");
        visit.setTimestamp(System.currentTimeMillis());
        visit.setOs("Windows");
        visit.setBrowser("Chrome");
        visit.setDevice("Desktop");
        visit.setLandingPage("/home");
        
        List<EventHistory> eventHistory = new ArrayList<>();
        EventHistory event1 = new EventHistory();
        event1.setEventId("event-001");
        event1.setEventName("login");
        event1.setEventType("action");
        event1.setTimestamp(System.currentTimeMillis() - 10000);
        eventHistory.add(event1);
        
        return new EnrichedEvent(userEvent, userProfile, visit, eventHistory);
    }
}
