package com.example.flink.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Event parameters containing custom parameters and UTM tracking information
 */
public class EventParameters implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("custom_params")
    private Map<String, Object> customParams = new HashMap<>();

    @JsonProperty("utm_source")
    private String utmSource;

    @JsonProperty("utm_campaign")
    private String utmCampaign;

    @JsonProperty("utm_content")
    private String utmContent;

    @JsonProperty("utm_medium")
    private String utmMedium;

    @JsonProperty("utm_term")
    private String utmTerm;

    @JsonProperty("utm_id")
    private String utmId;

    public EventParameters() {
    }

    public Map<String, Object> getCustomParams() {
        return customParams;
    }

    public void setCustomParams(Map<String, Object> customParams) {
        this.customParams = customParams;
    }

    public String getUtmSource() {
        return utmSource;
    }

    public void setUtmSource(String utmSource) {
        this.utmSource = utmSource;
    }

    public String getUtmCampaign() {
        return utmCampaign;
    }

    public void setUtmCampaign(String utmCampaign) {
        this.utmCampaign = utmCampaign;
    }

    public String getUtmContent() {
        return utmContent;
    }

    public void setUtmContent(String utmContent) {
        this.utmContent = utmContent;
    }

    public String getUtmMedium() {
        return utmMedium;
    }

    public void setUtmMedium(String utmMedium) {
        this.utmMedium = utmMedium;
    }

    public String getUtmTerm() {
        return utmTerm;
    }

    public void setUtmTerm(String utmTerm) {
        this.utmTerm = utmTerm;
    }

    public String getUtmId() {
        return utmId;
    }

    public void setUtmId(String utmId) {
        this.utmId = utmId;
    }

    @Override
    public String toString() {
        return "EventParameters{" +
                "customParams=" + customParams +
                ", utmSource='" + utmSource + '\'' +
                ", utmCampaign='" + utmCampaign + '\'' +
                ", utmContent='" + utmContent + '\'' +
                ", utmMedium='" + utmMedium + '\'' +
                ", utmTerm='" + utmTerm + '\'' +
                ", utmId='" + utmId + '\'' +
                '}';
    }
}
