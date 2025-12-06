package com.example.backend.dto;

public class PomodoroStatsResponse {
    private PomodoroPeriodStats today;
    private PomodoroPeriodStats week;
    private PomodoroPeriodStats month;
    private PomodoroPeriodStats total;

    public PomodoroPeriodStats getToday() {
        return today;
    }

    public void setToday(PomodoroPeriodStats today) {
        this.today = today;
    }

    public PomodoroPeriodStats getWeek() {
        return week;
    }

    public void setWeek(PomodoroPeriodStats week) {
        this.week = week;
    }

    public PomodoroPeriodStats getMonth() {
        return month;
    }

    public void setMonth(PomodoroPeriodStats month) {
        this.month = month;
    }

    public PomodoroPeriodStats getTotal() {
        return total;
    }

    public void setTotal(PomodoroPeriodStats total) {
        this.total = total;
    }
}


