package com.example.flink.action;

import com.example.flink.model.EnrichedEvent;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;

/**
 * RichMapFunction that executes an action handler for each enriched event.
 * 
 * This function is serializable and can be used in Flink's distributed environment.
 */
public class ActionExecutorFunction extends RichMapFunction<EnrichedEvent, EnrichedEvent> {
    private static final long serialVersionUID = 1L;
    
    private final boolean isDebugMode;
    private final String webhookUrl;
    private final int timeoutMs;
    
    private transient ActionHandler actionHandler;
    
    public ActionExecutorFunction(boolean isDebugMode, String webhookUrl, int timeoutMs) {
        this.isDebugMode = isDebugMode;
        this.webhookUrl = webhookUrl;
        this.timeoutMs = timeoutMs;
    }
    
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        // Initialize action handler here to avoid serialization issues
        if (isDebugMode) {
            this.actionHandler = new DebugPrintActionHandler();
        } else {
            this.actionHandler = new WebhookActionHandler(webhookUrl, timeoutMs);
        }
    }
    
    @Override
    public EnrichedEvent map(EnrichedEvent event) throws Exception {
        actionHandler.execute(event);
        return event;
    }
}
