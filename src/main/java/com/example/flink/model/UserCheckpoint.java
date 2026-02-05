package com.example.flink.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User checkpoint storing user profile, visit, and event history
 * Used by Flink Keyed State with TTL for automatic cleanup
 */
public class UserCheckpoint implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private UserProfile userProfile;
    private Visit currentVisit;
    private List<EventHistory> eventHistory;
    private long lastActivityTime;

    public UserCheckpoint() {
    }

    public UserCheckpoint(String userId, UserProfile userProfile, Visit currentVisit, List<EventHistory> eventHistory) {
        this.userId = userId;
        this.userProfile = userProfile;
        this.currentVisit = currentVisit;
        this.eventHistory = eventHistory;
        this.lastActivityTime = System.currentTimeMillis();
    }

    /**
     * Create an empty checkpoint for a user when API call fails
     */
    public static UserCheckpoint empty(String userId) {
        UserCheckpoint checkpoint = new UserCheckpoint();
        checkpoint.setUserId(userId);
        checkpoint.setUserProfile(new UserProfile());
        checkpoint.setCurrentVisit(null);
        checkpoint.setEventHistory(new ArrayList<>());
        checkpoint.setLastActivityTime(System.currentTimeMillis());
        return checkpoint;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public long getLastActivityTime() {
        return lastActivityTime;
    }

    public void setLastActivityTime(long lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }

    @Override
    public String toString() {
        return "UserCheckpoint{" +
                "userId='" + userId + '\'' +
                ", userProfile=" + userProfile +
                ", currentVisit=" + currentVisit +
                ", eventHistory=" + eventHistory +
                ", lastActivityTime=" + lastActivityTime +
                '}';
    }
}
