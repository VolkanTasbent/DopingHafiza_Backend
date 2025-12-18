package com.example.backend.dto;

import com.example.backend.model.UserActivity;
import java.time.Instant;
import java.util.Map;

public class ActivityResponse {
    private Long id;
    private String activityType;
    private String activityTitle;
    private String activitySubtitle;
    private String activityIcon;
    private Long dersId;
    private Long konuId;
    private Instant createdAt;
    private Map<String, Object> metadata;

    public static ActivityResponse from(UserActivity activity) {
        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setActivityType(activity.getActivityType());
        response.setActivityTitle(activity.getActivityTitle());
        response.setActivitySubtitle(activity.getActivitySubtitle());
        response.setActivityIcon(activity.getActivityIcon());
        response.setDersId(activity.getDersId());
        response.setKonuId(activity.getKonuId());
        response.setCreatedAt(activity.getCreatedAt());
        response.setMetadata(activity.getMetadata());
        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public String getActivitySubtitle() {
        return activitySubtitle;
    }

    public void setActivitySubtitle(String activitySubtitle) {
        this.activitySubtitle = activitySubtitle;
    }

    public String getActivityIcon() {
        return activityIcon;
    }

    public void setActivityIcon(String activityIcon) {
        this.activityIcon = activityIcon;
    }

    public Long getDersId() {
        return dersId;
    }

    public void setDersId(Long dersId) {
        this.dersId = dersId;
    }

    public Long getKonuId() {
        return konuId;
    }

    public void setKonuId(Long konuId) {
        this.konuId = konuId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}







