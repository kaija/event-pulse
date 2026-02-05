package com.example.flink.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Application configuration loaded from application.yml
 */
public class AppConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("kafka")
    private KafkaConfig kafka;

    @JsonProperty("profile-api")
    private ProfileApiConfig profileApi;

    @JsonProperty("filter")
    private FilterConfig filter;

    @JsonProperty("actions")
    private ActionsConfig actions;

    @JsonProperty("flink")
    private FlinkConfig flink;

    public AppConfig() {
    }

    public KafkaConfig getKafka() {
        return kafka;
    }

    public void setKafka(KafkaConfig kafka) {
        this.kafka = kafka;
    }

    public ProfileApiConfig getProfileApi() {
        return profileApi;
    }

    public void setProfileApi(ProfileApiConfig profileApi) {
        this.profileApi = profileApi;
    }

    public FilterConfig getFilter() {
        return filter;
    }

    public void setFilter(FilterConfig filter) {
        this.filter = filter;
    }

    public ActionsConfig getActions() {
        return actions;
    }

    public void setActions(ActionsConfig actions) {
        this.actions = actions;
    }

    public FlinkConfig getFlink() {
        return flink;
    }

    public void setFlink(FlinkConfig flink) {
        this.flink = flink;
    }

    @Override
    public String toString() {
        return "AppConfig{" +
                "kafka=" + kafka +
                ", profileApi=" + profileApi +
                ", filter=" + filter +
                ", actions=" + actions +
                ", flink=" + flink +
                '}';
    }
}
