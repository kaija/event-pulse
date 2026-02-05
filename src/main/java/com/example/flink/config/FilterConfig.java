package com.example.flink.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Filter configuration
 */
public class FilterConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("script-path")
    private String scriptPath;

    public FilterConfig() {
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    @Override
    public String toString() {
        return "FilterConfig{" +
                "scriptPath='" + scriptPath + '\'' +
                '}';
    }
}
