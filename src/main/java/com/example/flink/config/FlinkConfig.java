package com.example.flink.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Flink configuration
 */
public class FlinkConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("parallelism")
    private int parallelism;

    @JsonProperty("checkpoint-interval-ms")
    private long checkpointIntervalMs;

    @JsonProperty("state-backend")
    private String stateBackend;

    @JsonProperty("state-ttl-minutes")
    private int stateTtlMinutes;

    public FlinkConfig() {
    }

    public int getParallelism() {
        return parallelism;
    }

    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }

    public long getCheckpointIntervalMs() {
        return checkpointIntervalMs;
    }

    public void setCheckpointIntervalMs(long checkpointIntervalMs) {
        this.checkpointIntervalMs = checkpointIntervalMs;
    }

    public String getStateBackend() {
        return stateBackend;
    }

    public void setStateBackend(String stateBackend) {
        this.stateBackend = stateBackend;
    }

    public int getStateTtlMinutes() {
        return stateTtlMinutes;
    }

    public void setStateTtlMinutes(int stateTtlMinutes) {
        this.stateTtlMinutes = stateTtlMinutes;
    }

    @Override
    public String toString() {
        return "FlinkConfig{" +
                "parallelism=" + parallelism +
                ", checkpointIntervalMs=" + checkpointIntervalMs +
                ", stateBackend='" + stateBackend + '\'' +
                ", stateTtlMinutes=" + stateTtlMinutes +
                '}';
    }
}
