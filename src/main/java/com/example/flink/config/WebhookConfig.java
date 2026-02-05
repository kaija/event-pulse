package com.example.flink.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Webhook action configuration
 */
public class WebhookConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("url")
    private String url;

    @JsonProperty("timeout-ms")
    private int timeoutMs;

    public WebhookConfig() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    @Override
    public String toString() {
        return "WebhookConfig{" +
                "url='" + url + '\'' +
                ", timeoutMs=" + timeoutMs +
                '}';
    }
}
