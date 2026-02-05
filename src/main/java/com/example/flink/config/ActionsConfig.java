package com.example.flink.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Actions configuration
 */
public class ActionsConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("webhook")
    private WebhookConfig webhook;

    @JsonProperty("debug")
    private DebugConfig debug;

    public ActionsConfig() {
    }

    public WebhookConfig getWebhook() {
        return webhook;
    }

    public void setWebhook(WebhookConfig webhook) {
        this.webhook = webhook;
    }

    public DebugConfig getDebug() {
        return debug;
    }

    public void setDebug(DebugConfig debug) {
        this.debug = debug;
    }

    @Override
    public String toString() {
        return "ActionsConfig{" +
                "webhook=" + webhook +
                ", debug=" + debug +
                '}';
    }
}
