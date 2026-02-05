package com.example.flink.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * User tracking event received from Kafka
 */
public class UserEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("domain")
    private String domain;

    @JsonProperty("is_first_in_visit")
    private boolean isFirstInVisit;

    @JsonProperty("is_last_in_visit")
    private boolean isLastInVisit;

    @JsonProperty("is_first_event")
    private boolean isFirstEvent;

    @JsonProperty("is_current")
    private boolean isCurrent;

    @JsonProperty("event_name")
    private String eventName;

    @JsonProperty("event_displayname")
    private String eventDisplayname;

    @JsonProperty("integration")
    private String integration;

    @JsonProperty("app")
    private String app;

    @JsonProperty("platform")
    private String platform;

    @JsonProperty("is_https")
    private boolean isHttps;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("duration")
    private long duration;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("triggerable")
    private boolean triggerable;

    @JsonProperty("parameters")
    private EventParameters parameters;

    public UserEvent() {
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isFirstInVisit() {
        return isFirstInVisit;
    }

    public void setFirstInVisit(boolean firstInVisit) {
        isFirstInVisit = firstInVisit;
    }

    public boolean isLastInVisit() {
        return isLastInVisit;
    }

    public void setLastInVisit(boolean lastInVisit) {
        isLastInVisit = lastInVisit;
    }

    public boolean isFirstEvent() {
        return isFirstEvent;
    }

    public void setFirstEvent(boolean firstEvent) {
        isFirstEvent = firstEvent;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDisplayname() {
        return eventDisplayname;
    }

    public void setEventDisplayname(String eventDisplayname) {
        this.eventDisplayname = eventDisplayname;
    }

    public String getIntegration() {
        return integration;
    }

    public void setIntegration(String integration) {
        this.integration = integration;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public boolean isHttps() {
        return isHttps;
    }

    public void setHttps(boolean https) {
        isHttps = https;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isTriggerable() {
        return triggerable;
    }

    public void setTriggerable(boolean triggerable) {
        this.triggerable = triggerable;
    }

    public EventParameters getParameters() {
        return parameters;
    }

    public void setParameters(EventParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "UserEvent{" +
                "eventId='" + eventId + '\'' +
                ", userId='" + userId + '\'' +
                ", domain='" + domain + '\'' +
                ", isFirstInVisit=" + isFirstInVisit +
                ", isLastInVisit=" + isLastInVisit +
                ", isFirstEvent=" + isFirstEvent +
                ", isCurrent=" + isCurrent +
                ", eventName='" + eventName + '\'' +
                ", eventDisplayname='" + eventDisplayname + '\'' +
                ", integration='" + integration + '\'' +
                ", app='" + app + '\'' +
                ", platform='" + platform + '\'' +
                ", isHttps=" + isHttps +
                ", eventType='" + eventType + '\'' +
                ", duration=" + duration +
                ", timestamp=" + timestamp +
                ", triggerable=" + triggerable +
                ", parameters=" + parameters +
                '}';
    }
}
