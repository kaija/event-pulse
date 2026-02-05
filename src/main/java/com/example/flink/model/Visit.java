package com.example.flink.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * User visit information including device, browser, and referrer details
 */
public class Visit implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("os")
    private String os;

    @JsonProperty("browser")
    private String browser;

    @JsonProperty("device")
    private String device;

    @JsonProperty("landing_page")
    private String landingPage;

    @JsonProperty("referrer_type")
    private String referrerType;

    @JsonProperty("referrer_url")
    private String referrerUrl;

    @JsonProperty("referrer_query")
    private String referrerQuery;

    @JsonProperty("duration")
    private long duration;

    @JsonProperty("actions")
    private int actions;

    @JsonProperty("is_first_visit")
    private boolean isFirstVisit;

    @JsonProperty("screen")
    private String screen;

    @JsonProperty("ip")
    private String ip;

    public Visit() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getLandingPage() {
        return landingPage;
    }

    public void setLandingPage(String landingPage) {
        this.landingPage = landingPage;
    }

    public String getReferrerType() {
        return referrerType;
    }

    public void setReferrerType(String referrerType) {
        this.referrerType = referrerType;
    }

    public String getReferrerUrl() {
        return referrerUrl;
    }

    public void setReferrerUrl(String referrerUrl) {
        this.referrerUrl = referrerUrl;
    }

    public String getReferrerQuery() {
        return referrerQuery;
    }

    public void setReferrerQuery(String referrerQuery) {
        this.referrerQuery = referrerQuery;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getActions() {
        return actions;
    }

    public void setActions(int actions) {
        this.actions = actions;
    }

    public boolean isFirstVisit() {
        return isFirstVisit;
    }

    public void setFirstVisit(boolean firstVisit) {
        isFirstVisit = firstVisit;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "Visit{" +
                "uuid='" + uuid + '\'' +
                ", timestamp=" + timestamp +
                ", os='" + os + '\'' +
                ", browser='" + browser + '\'' +
                ", device='" + device + '\'' +
                ", landingPage='" + landingPage + '\'' +
                ", referrerType='" + referrerType + '\'' +
                ", referrerUrl='" + referrerUrl + '\'' +
                ", referrerQuery='" + referrerQuery + '\'' +
                ", duration=" + duration +
                ", actions=" + actions +
                ", isFirstVisit=" + isFirstVisit +
                ", screen='" + screen + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }
}
