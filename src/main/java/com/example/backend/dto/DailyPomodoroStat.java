package com.example.backend.dto;

public class DailyPomodoroStat {
    private String date;
    private int count;
    private int minutes;

    public DailyPomodoroStat() {
    }

    public DailyPomodoroStat(String date, int count, int minutes) {
        this.date = date;
        this.count = count;
        this.minutes = minutes;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }
}



