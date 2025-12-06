package com.example.backend.dto;

import java.util.List;

public class RecentActivitiesResponse {
    private List<ActivityResponse> activities;

    public RecentActivitiesResponse() {
    }

    public RecentActivitiesResponse(List<ActivityResponse> activities) {
        this.activities = activities;
    }

    public List<ActivityResponse> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityResponse> activities) {
        this.activities = activities;
    }
}

