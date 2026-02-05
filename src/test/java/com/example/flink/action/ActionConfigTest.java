package com.example.flink.action;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ActionConfig 單元測試
 */
class ActionConfigTest {
    
    @Test
    void testConstructorAndGetters() {
        // Arrange
        ActionType actionType = ActionType.WEBHOOK;
        Map<String, String> parameters = new HashMap<>();
        parameters.put("url", "http://example.com/webhook");
        parameters.put("timeout", "3000");
        
        // Act
        ActionConfig config = new ActionConfig(actionType, parameters);
        
        // Assert
        assertEquals(ActionType.WEBHOOK, config.getActionType());
        assertEquals(parameters, config.getParameters());
        assertEquals("http://example.com/webhook", config.getParameters().get("url"));
    }
    
    @Test
    void testSetters() {
        // Arrange
        ActionConfig config = new ActionConfig();
        ActionType actionType = ActionType.DEBUG_PRINT;
        Map<String, String> parameters = new HashMap<>();
        parameters.put("format", "json");
        
        // Act
        config.setActionType(actionType);
        config.setParameters(parameters);
        
        // Assert
        assertEquals(ActionType.DEBUG_PRINT, config.getActionType());
        assertEquals(parameters, config.getParameters());
    }
    
    @Test
    void testToString() {
        // Arrange
        ActionType actionType = ActionType.WEBHOOK;
        Map<String, String> parameters = new HashMap<>();
        parameters.put("url", "http://example.com/webhook");
        ActionConfig config = new ActionConfig(actionType, parameters);
        
        // Act
        String result = config.toString();
        
        // Assert
        assertTrue(result.contains("WEBHOOK"));
        assertTrue(result.contains("url"));
    }
    
    @Test
    void testEmptyConstructor() {
        // Act
        ActionConfig config = new ActionConfig();
        
        // Assert
        assertNull(config.getActionType());
        assertNull(config.getParameters());
    }
}
