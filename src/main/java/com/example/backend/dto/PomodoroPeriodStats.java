package com.example.backend.dto;

public class PomodoroPeriodStats {
    private Integer count;
    private Integer minutes;

    public PomodoroPeriodStats() {
    }

    public PomodoroPeriodStats(Integer count, Integer minutes) {
        this.count = count;
        this.minutes = minutes;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }
}








