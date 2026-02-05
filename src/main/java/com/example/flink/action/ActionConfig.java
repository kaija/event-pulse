package com.example.flink.action;

import java.util.Map;

/**
 * 動作配置資料類別
 * 
 * 定義事件觸發時要執行的動作類型和相關參數
 */
public class ActionConfig {
    private ActionType actionType;
    private Map<String, String> parameters;
    
    public ActionConfig() {
    }
    
    public ActionConfig(ActionType actionType, Map<String, String> parameters) {
        this.actionType = actionType;
        this.parameters = parameters;
    }
    
    public ActionType getActionType() {
        return actionType;
    }
    
    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
    
    @Override
    public String toString() {
        return "ActionConfig{" +
                "actionType=" + actionType +
                ", parameters=" + parameters +
                '}';
    }
}
