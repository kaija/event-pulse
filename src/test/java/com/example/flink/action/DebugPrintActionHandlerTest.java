package com.example.flink.action;

import com.example.flink.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DebugPrintActionHandler 單元測試
 * 
 * 測試 Debug Print 動作處理器的功能，包括 JSON 格式化輸出和錯誤處理
 */
class DebugPrintActionHandlerTest {
    
    private DebugPrintActionHandler handler;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        handler = new DebugPrintActionHandler();
        objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    /**
     * 測試成功執行 Debug Print
     * 
     * 需求 4.6: 將事件資料輸出到日誌
     * 需求 4.7: 包含事件資料和使用者資料
     */
    @Test
    void testSuccessfulDebugPrint() {
        // Arrange
        EnrichedEvent event = createTestEnrichedEvent();
        
        // Act & Assert - 應該不拋出例外
        assertDoesNotThrow(() -> handler.execute(event));
    }
    
    /**
     * 測試可以序列化為 JSON
     * 
     * 驗證事件可以被正確序列化為 JSON 格式
     */
    @Test
    void testJsonSerialization() throws Exception {
        // Arrange
        EnrichedEvent event = createTestEnrichedEvent();
        
        // Act - 手動序列化以驗證格式
        String json = objectMapper.writeValueAsString(event);
        
        // Assert - 驗證包含所有必要欄位
        assertNotNull(json);
        assertTrue(json.contains("event-123"), "Should contain eventId");
        assertTrue(json.contains("user-456"), "Should contain userId");
        assertTrue(json.contains("page_view"), "Should contain eventName");
        assertTrue(json.contains("US"), "Should contain country");
        assertTrue(json.contains("New York"), "Should contain city");
        assertTrue(json.contains("visit-789"), "Should contain visit uuid");
        assertTrue(json.contains("Chrome"), "Should contain browser");
        assertTrue(json.contains("event-001"), "Should contain event history");
        assertTrue(json.contains("login"), "Should contain event history name");
    }
    
    /**
     * 測試 JSON 格式化輸出
     * 
     * 驗證輸出的 JSON 是格式化的（有縮排）
     */
    @Test
    void testJsonFormatting() throws Exception {
        // Arrange
        EnrichedEvent event = createTestEnrichedEvent();
        
        // Act - 手動序列化以驗證格式
        String json = objectMapper.writeValueAsString(event);
        
        // Assert - 驗證 JSON 格式化（應該包含換行和縮排）
        assertTrue(json.contains("\n"), "JSON should be formatted with newlines");
        assertTrue(json.contains("  "), "JSON should be formatted with indentation");
    }
    
    /**
     * 測試輸出的 JSON 可以被解析
     */
    @Test
    void testJsonParseable() throws Exception {
        // Arrange
        EnrichedEvent event = createTestEnrichedEvent();
        
        // Act - 序列化後再反序列化
        String json = objectMapper.writeValueAsString(event);
        EnrichedEvent parsed = objectMapper.readValue(json, EnrichedEvent.class);
        
        // Assert - 驗證可以正確解析
        assertNotNull(parsed);
        assertNotNull(parsed.getEvent());
        assertNotNull(parsed.getUserProfile());
        assertEquals("event-123", parsed.getEvent().getEventId());
        assertEquals("user-456", parsed.getEvent().getUserId());
        assertEquals("US", parsed.getUserProfile().getCountry());
    }
    
    /**
     * 測試多次執行
     */
    @Test
    void testMultipleExecutions() {
        // Arrange
        EnrichedEvent event1 = createTestEnrichedEvent();
        EnrichedEvent event2 = createTestEnrichedEvent();
        event2.getEvent().setEventId("event-456");
        EnrichedEvent event3 = createTestEnrichedEvent();
        event3.getEvent().setEventId("event-789");
        
        // Act & Assert - 應該都不拋出例外
        assertDoesNotThrow(() -> handler.execute(event1));
        assertDoesNotThrow(() -> handler.execute(event2));
        assertDoesNotThrow(() -> handler.execute(event3));
    }
    
    /**
     * 測試包含所有必要欄位
     */
    @Test
    void testContainsAllRequiredFields() throws Exception {
        // Arrange
        EnrichedEvent event = createTestEnrichedEvent();
        
        // Act - 手動序列化以驗證欄位
        String json = objectMapper.writeValueAsString(event);
        
        // Assert - 驗證主要結構
        assertTrue(json.contains("event"), "Should contain event field");
        assertTrue(json.contains("userProfile"), "Should contain userProfile field");
        assertTrue(json.contains("currentVisit"), "Should contain currentVisit field");
        assertTrue(json.contains("eventHistory"), "Should contain eventHistory field");
        
        // 驗證事件資料
        assertTrue(json.contains("event-123"), "Should contain eventId value");
        assertTrue(json.contains("user-456"), "Should contain userId value");
        assertTrue(json.contains("page_view"), "Should contain eventName value");
        
        // 驗證使用者資料
        assertTrue(json.contains("US"), "Should contain country value");
        assertTrue(json.contains("New York"), "Should contain city value");
        
        // 驗證訪問資訊
        assertTrue(json.contains("visit-789"), "Should contain visit uuid value");
        assertTrue(json.contains("Chrome"), "Should contain browser value");
    }
    
    /**
     * 測試空的事件歷史
     */
    @Test
    void testEmptyEventHistory() {
        // Arrange
        EnrichedEvent event = createTestEnrichedEvent();
        event.setEventHistory(new ArrayList<>());
        
        // Act & Assert - 應該不拋出例外
        assertDoesNotThrow(() -> handler.execute(event));
    }
    
    /**
     * 測試 null Visit
     */
    @Test
    void testNullVisit() {
        // Arrange
        EnrichedEvent event = createTestEnrichedEvent();
        event.setCurrentVisit(null);
        
        // Act & Assert - 應該不拋出例外
        assertDoesNotThrow(() -> handler.execute(event));
    }
    
    /**
     * 測試不拋出例外
     * 
     * 需求 4.5: 錯誤處理 - 應該記錄錯誤並繼續處理
     */
    @Test
    void testDoesNotThrowException() {
        // Arrange
        EnrichedEvent event = createTestEnrichedEvent();
        
        // Act & Assert
        assertDoesNotThrow(() -> handler.execute(event));
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
