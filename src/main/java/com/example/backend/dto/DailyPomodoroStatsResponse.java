package com.example.backend.dto;

import java.util.List;

public class DailyPomodoroStatsResponse {
    private List<DailyPomodoroStat> dailyStats;

    public DailyPomodoroStatsResponse() {
    }

    public DailyPomodoroStatsResponse(List<DailyPomodoroStat> dailyStats) {
        this.dailyStats = dailyStats;
    }

    public List<DailyPomodoroStat> getDailyStats() {
        return dailyStats;
    }

    public void setDailyStats(List<DailyPomodoroStat> dailyStats) {
        this.dailyStats = dailyStats;
    }
}



