package com.example.flink.action;

/**
 * 動作類型列舉
 * 
 * 需求 4.1: THE Event_Trigger SHALL 支援 Webhook 動作類型
 * 需求 4.2: THE Event_Trigger SHALL 支援 Debug Print 動作類型
 */
public enum ActionType {
    /**
     * Webhook 動作 - 發送 HTTP POST 請求到指定的 URL
     */
    WEBHOOK,
    
    /**
     * Debug Print 動作 - 將事件資料輸出到日誌
     */
    DEBUG_PRINT
}
