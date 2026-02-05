package com.example.flink.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Historical event record for a user
 */
public class EventHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("event_name")
    private String eventName;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("parameters")
    private EventParameters parameters;

    public EventHistory() {
    }

    public EventHistory(String eventId, String eventName, String eventType, long timestamp, EventParameters parameters) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.parameters = parameters;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public EventParameters getParameters() {
        return parameters;
    }

    public void setParameters(EventParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "EventHistory{" +
                "eventId='" + eventId + '\'' +
                ", eventName='" + eventName + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                ", parameters=" + parameters +
                '}';
    }
}
