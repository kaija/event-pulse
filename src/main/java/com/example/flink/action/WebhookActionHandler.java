package com.example.flink.action;

import com.example.flink.model.EnrichedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Webhook 動作處理器
 * 
 * 需求 4.3: WHEN 事件觸發 Webhook 動作時，THE Action_Handler SHALL 發送 HTTP POST 請求到指定的 URL
 * 需求 4.4: WHEN Webhook 請求成功時，THE Action_Handler SHALL 記錄成功狀態
 * 需求 4.5: WHEN Webhook 請求失敗時，THE Action_Handler SHALL 記錄錯誤並繼續處理
 * 需求 4.7: THE Action_Handler SHALL 在請求內容中包含事件資料和使用者資料
 * 
 * 發送包含事件資料和使用者資料的 HTTP POST 請求到指定的 Webhook URL
 */
public class WebhookActionHandler extends ActionHandler {
    private static final Logger logger = LoggerFactory.getLogger(WebhookActionHandler.class);
    
    private final String webhookUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final int timeoutMs;
    
    /**
     * 建立 WebhookActionHandler
     * 
     * @param webhookUrl Webhook URL
     */
    public WebhookActionHandler(String webhookUrl) {
        this(webhookUrl, 3000); // 預設 3 秒逾時
    }
    
    /**
     * 建立 WebhookActionHandler 並指定逾時時間
     * 
     * @param webhookUrl Webhook URL
     * @param timeoutMs HTTP 請求逾時時間（毫秒）
     */
    public WebhookActionHandler(String webhookUrl, int timeoutMs) {
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Webhook URL cannot be null or empty");
        }
        
        this.webhookUrl = webhookUrl;
        this.timeoutMs = timeoutMs;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(timeoutMs))
            .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 執行 Webhook 動作
     * 
     * 需求 4.3: 發送 HTTP POST 請求到指定的 URL
     * 需求 4.7: 在請求內容中包含事件資料和使用者資料
     * 
     * @param event 豐富化的事件物件
     */
    @Override
    public void execute(EnrichedEvent event) {
        try {
            // 建立 Webhook 請求內容（需求 4.7）
            WebhookPayload payload = new WebhookPayload(event, System.currentTimeMillis());
            String jsonBody = objectMapper.writeValueAsString(payload);
            
            // 建立 HTTP POST 請求（需求 4.3）
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofMillis(timeoutMs))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
            
            // 發送請求
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            // 檢查回應狀態（需求 4.4, 4.5）
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                // 需求 4.4: 記錄成功狀態
                logger.info("Webhook sent successfully for event: {}, status: {}", 
                           event.getEvent().getEventId(), response.statusCode());
            } else {
                // 需求 4.5: 記錄錯誤並繼續處理
                logger.error("Webhook failed with status {} for event: {}, response: {}", 
                            response.statusCode(), event.getEvent().getEventId(), response.body());
            }
        } catch (JsonProcessingException e) {
            // 需求 4.5: 記錄錯誤並繼續處理
            logger.error("Failed to serialize webhook payload for event: {}", 
                        event.getEvent().getEventId(), e);
        } catch (IOException e) {
            // 需求 4.5: 記錄錯誤並繼續處理（網路錯誤）
            logger.error("Failed to send webhook for event: {} due to IO error", 
                        event.getEvent().getEventId(), e);
        } catch (InterruptedException e) {
            // 需求 4.5: 記錄錯誤並繼續處理（逾時或中斷）
            logger.error("Webhook request interrupted for event: {}", 
                        event.getEvent().getEventId(), e);
            Thread.currentThread().interrupt(); // 恢復中斷狀態
        } catch (Exception e) {
            // 需求 4.5: 記錄錯誤並繼續處理（其他錯誤）
            logger.error("Unexpected error sending webhook for event: {}", 
                        event.getEvent().getEventId(), e);
        }
    }
    
    /**
     * 取得 Webhook URL
     * 
     * @return Webhook URL
     */
    public String getWebhookUrl() {
        return webhookUrl;
    }
    
    /**
     * 取得逾時時間
     * 
     * @return 逾時時間（毫秒）
     */
    public int getTimeoutMs() {
        return timeoutMs;
    }
}
