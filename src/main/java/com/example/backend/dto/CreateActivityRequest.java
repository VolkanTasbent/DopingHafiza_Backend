package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Map;

public class CreateActivityRequest {
    @NotBlank(message = "Activity type is required")
    @Pattern(regexp = "soru_cozme|video_izleme|konu_calisma|ders_tamamlama|pomodoro", 
             message = "Activity type must be one of: soru_cozme, video_izleme, konu_calisma, ders_tamamlama, pomodoro")
    private String activityType;

    @NotBlank(message = "Activity title is required")
    private String activityTitle;

    private String activitySubtitle;

    @Pattern(regexp = "document|video|book|grid|abc", 
             message = "Activity icon must be one of: document, video, book, grid, abc")
    private String activityIcon;

    private Long dersId;

    private Long konuId;

    private Long raporId;

    private Map<String, Object> metadata;

    // Getters and Setters
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

    public Long getRaporId() {
        return raporId;
    }

    public void setRaporId(Long raporId) {
        this.raporId = raporId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}







