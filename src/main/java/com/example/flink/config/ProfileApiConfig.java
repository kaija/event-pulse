package com.example.flink.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Profile API configuration
 */
public class ProfileApiConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("base-url")
    private String baseUrl;

    @JsonProperty("timeout-ms")
    private int timeoutMs;

    @JsonProperty("retry-attempts")
    private int retryAttempts;

    public ProfileApiConfig() {
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    @Override
    public String toString() {
        return "ProfileApiConfig{" +
                "baseUrl='" + baseUrl + '\'' +
                ", timeoutMs=" + timeoutMs +
                ", retryAttempts=" + retryAttempts +
                '}';
    }
}
