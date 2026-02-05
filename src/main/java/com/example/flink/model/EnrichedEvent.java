package com.example.flink.model;

import java.io.Serializable;
import java.util.List;

/**
 * Event enriched with user profile, visit, and event history
 */
public class EnrichedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private UserEvent event;
    private UserProfile userProfile;
    private Visit currentVisit;
    private List<EventHistory> eventHistory;

    public EnrichedEvent() {
    }

    public EnrichedEvent(UserEvent event, UserProfile userProfile, Visit currentVisit, List<EventHistory> eventHistory) {
        this.event = event;
        this.userProfile = userProfile;
        this.currentVisit = currentVisit;
        this.eventHistory = eventHistory;
    }

    public UserEvent getEvent() {
        return event;
    }

    public void setEvent(UserEvent event) {
        this.event = event;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public Visit getCurrentVisit() {
        return currentVisit;
    }

    public void setCurrentVisit(Visit currentVisit) {
        this.currentVisit = currentVisit;
    }

    public List<EventHistory> getEventHistory() {
        return eventHistory;
    }

    public void setEventHistory(List<EventHistory> eventHistory) {
        this.eventHistory = eventHistory;
    }

    @Override
    public String toString() {
        return "EnrichedEvent{" +
                "event=" + event +
                ", userProfile=" + userProfile +
                ", currentVisit=" + currentVisit +
                ", eventHistory=" + eventHistory +
                '}';
    }
}
