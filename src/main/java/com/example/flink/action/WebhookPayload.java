package com.example.flink.action;

import com.example.flink.model.EnrichedEvent;
import com.example.flink.model.EventHistory;
import com.example.flink.model.UserEvent;
import com.example.flink.model.UserProfile;
import com.example.flink.model.Visit;

import java.util.List;

/**
 * Webhook 請求內容資料類別
 * 
 * 需求 4.7: THE Action_Handler SHALL 在請求內容中包含事件資料和使用者資料
 * 
 * 包含完整的事件資料、使用者資料、訪問資訊和事件歷史
 */
public class WebhookPayload {
    private UserEvent event;
    private UserProfile userProfile;
    private Visit currentVisit;
    private List<EventHistory> eventHistory;
    private long triggeredAt;
    
    public WebhookPayload() {
    }
    
    /**
     * 從 EnrichedEvent 建立 WebhookPayload
     * 
     * @param enrichedEvent 豐富化的事件物件
     * @param triggeredAt 觸發時間戳記（毫秒）
     */
    public WebhookPayload(EnrichedEvent enrichedEvent, long triggeredAt) {
        this.event = enrichedEvent.getEvent();
        this.userProfile = enrichedEvent.getUserProfile();
        this.currentVisit = enrichedEvent.getCurrentVisit();
        this.eventHistory = enrichedEvent.getEventHistory();
        this.triggeredAt = triggeredAt;
    }
    
    public UserEvent getEvent() {
        return event;
    }
    
    public void setEvent(UserEvent event) {
        this.event = event;
    }
    
    public UserProfile getUserProfile() {
        return userProfile;
    }
    
    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
    
    public Visit getCurrentVisit() {
        return currentVisit;
    }
    
    public void setCurrentVisit(Visit currentVisit) {
        this.currentVisit = currentVisit;
    }
    
    public List<EventHistory> getEventHistory() {
        return eventHistory;
    }
    
    public void setEventHistory(List<EventHistory> eventHistory) {
        this.eventHistory = eventHistory;
    }
    
    public long getTriggeredAt() {
        return triggeredAt;
    }
    
    public void setTriggeredAt(long triggeredAt) {
        this.triggeredAt = triggeredAt;
    }
    
    @Override
    public String toString() {
        return "WebhookPayload{" +
                "event=" + event +
                ", userProfile=" + userProfile +
                ", currentVisit=" + currentVisit +
                ", eventHistory=" + eventHistory +
                ", triggeredAt=" + triggeredAt +
                '}';
    }
}
