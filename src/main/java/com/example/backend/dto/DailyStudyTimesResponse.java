package com.example.backend.dto;

import java.util.List;

public class DailyStudyTimesResponse {
    private List<DailyStudyTime> dailyTimes;

    public DailyStudyTimesResponse() {
    }

    public DailyStudyTimesResponse(List<DailyStudyTime> dailyTimes) {
        this.dailyTimes = dailyTimes;
    }

    public List<DailyStudyTime> getDailyTimes() {
        return dailyTimes;
    }

    public void setDailyTimes(List<DailyStudyTime> dailyTimes) {
        this.dailyTimes = dailyTimes;
    }
}







