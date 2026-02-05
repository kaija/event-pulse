package com.example.flink.action;

import com.example.flink.model.EnrichedEvent;

/**
 * 動作處理器抽象類別
 * 
 * 需求 4.1: THE Event_Trigger SHALL 支援 Webhook 動作類型
 * 需求 4.2: THE Event_Trigger SHALL 支援 Debug Print 動作類型
 * 
 * 定義事件動作的執行介面，具體的動作類型（Webhook 或 Debug Print）
 * 需要繼承此類別並實作 execute() 方法
 */
public abstract class ActionHandler implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 執行事件動作
     * 
     * @param event 豐富化的事件物件，包含事件資料、使用者資料、訪問資訊和事件歷史
     */
    public abstract void execute(EnrichedEvent event);
}
