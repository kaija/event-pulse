package com.example.flink.action;

import com.example.flink.model.EnrichedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Debug Print 動作處理器
 * 
 * 需求 4.6: WHEN 事件觸發 Debug Print 動作時，THE Action_Handler SHALL 將事件資料輸出到日誌
 * 需求 4.7: THE Action_Handler SHALL 在請求內容中包含事件資料和使用者資料
 * 
 * 將豐富化的事件資料以 JSON 格式輸出到日誌，用於除錯和監控
 */
public class DebugPrintActionHandler extends ActionHandler {
    private static final Logger logger = LoggerFactory.getLogger(DebugPrintActionHandler.class);
    
    private final ObjectMapper objectMapper;
    
    /**
     * 建立 DebugPrintActionHandler
     * 
     * 使用 Jackson ObjectMapper 並啟用格式化輸出（INDENT_OUTPUT）
     */
    public DebugPrintActionHandler() {
        this.objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    /**
     * 執行 Debug Print 動作
     * 
     * 需求 4.6: 將事件資料輸出到日誌
     * 需求 4.7: 包含事件資料和使用者資料
     * 
     * @param event 豐富化的事件物件
     */
    @Override
    public void execute(EnrichedEvent event) {
        try {
            // 將事件序列化為格式化的 JSON（需求 4.6, 4.7）
            String eventJson = objectMapper.writeValueAsString(event);
            
            // 輸出到日誌
            logger.info("DEBUG EVENT:\n{}", eventJson);
        } catch (JsonProcessingException e) {
            // 需求 8.1: 記錄錯誤訊息
            logger.error("Failed to serialize event for debug print: {}", 
                        event.getEvent().getEventId(), e);
        } catch (Exception e) {
            // 處理其他可能的錯誤
            logger.error("Unexpected error during debug print for event: {}", 
                        event.getEvent().getEventId(), e);
        }
    }
}
