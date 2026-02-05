package com.example.flink.api;

import com.example.flink.model.EventHistory;
import com.example.flink.model.UserProfile;
import com.example.flink.model.Visit;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 測試 ProfileApiClient 介面和 ApiException
 */
class ProfileApiClientTest {
    
    /**
     * 測試 ProfileApiClient 介面可以被實作
     */
    @Test
    void testProfileApiClientCanBeImplemented() {
        // 建立一個簡單的實作來驗證介面定義正確
        ProfileApiClient client = new ProfileApiClient() {
            @Override
            public UserProfile getUserProfile(String userId) throws ApiException {
                if (userId == null || userId.isEmpty()) {
                    throw new ApiException("User ID cannot be null or empty");
                }
                UserProfile profile = new UserProfile();
                profile.setUserId(userId);
                return profile;
            }
            
            @Override
            public Visit getCurrentVisit(String userId) throws ApiException {
                if (userId == null || userId.isEmpty()) {
                    throw new ApiException("User ID cannot be null or empty");
                }
                Visit visit = new Visit();
                return visit;
            }
            
            @Override
            public List<EventHistory> getEventHistory(String userId) throws ApiException {
                if (userId == null || userId.isEmpty()) {
                    throw new ApiException("User ID cannot be null or empty");
                }
                return new ArrayList<>();
            }
        };
        
        // 驗證介面方法可以被呼叫
        assertDoesNotThrow(() -> {
            UserProfile profile = client.getUserProfile("user123");
            assertNotNull(profile);
            assertEquals("user123", profile.getUserId());
        });
        
        assertDoesNotThrow(() -> {
            Visit visit = client.getCurrentVisit("user123");
            assertNotNull(visit);
        });
        
        assertDoesNotThrow(() -> {
            List<EventHistory> history = client.getEventHistory("user123");
            assertNotNull(history);
            assertTrue(history.isEmpty());
        });
    }
    
    /**
     * 測試 ApiException 可以正確拋出和捕獲
     */
    @Test
    void testApiExceptionCanBeThrownAndCaught() {
        ProfileApiClient client = new ProfileApiClient() {
            @Override
            public UserProfile getUserProfile(String userId) throws ApiException {
                throw new ApiException("API call failed");
            }
            
            @Override
            public Visit getCurrentVisit(String userId) throws ApiException {
                throw new ApiException("API call failed", new RuntimeException("Network error"));
            }
            
            @Override
            public List<EventHistory> getEventHistory(String userId) throws ApiException {
                throw new ApiException("API call failed");
            }
        };
        
        // 測試 ApiException 可以被拋出和捕獲
        ApiException exception1 = assertThrows(ApiException.class, () -> {
            client.getUserProfile("user123");
        });
        assertEquals("API call failed", exception1.getMessage());
        
        ApiException exception2 = assertThrows(ApiException.class, () -> {
            client.getCurrentVisit("user123");
        });
        assertEquals("API call failed", exception2.getMessage());
        assertNotNull(exception2.getCause());
        assertEquals("Network error", exception2.getCause().getMessage());
        
        ApiException exception3 = assertThrows(ApiException.class, () -> {
            client.getEventHistory("user123");
        });
        assertEquals("API call failed", exception3.getMessage());
    }
    
    /**
     * 測試 ApiException 建構子
     */
    @Test
    void testApiExceptionConstructors() {
        // 測試只有訊息的建構子
        ApiException exception1 = new ApiException("Test message");
        assertEquals("Test message", exception1.getMessage());
        assertNull(exception1.getCause());
        
        // 測試有訊息和原因的建構子
        RuntimeException cause = new RuntimeException("Root cause");
        ApiException exception2 = new ApiException("Test message with cause", cause);
        assertEquals("Test message with cause", exception2.getMessage());
        assertNotNull(exception2.getCause());
        assertEquals(cause, exception2.getCause());
    }
}
