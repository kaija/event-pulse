package com.example.flink.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Debug action configuration
 */
public class DebugConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("enabled")
    private boolean enabled;

    public DebugConfig() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "DebugConfig{" +
                "enabled=" + enabled +
                '}';
    }
}
