package com.example.backend.dto;

public class DailyStudyTime {
    private String date; // YYYY-MM-DD formatı
    private Integer totalMinutes;
    private Integer hours;
    private Integer minutes;
    private Integer soruCozmeMinutes;
    private Integer pomodoroMinutes;
    private Integer soruCozmeSessions;
    private Integer pomodoroSessions;

    // Getters and Setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(Integer totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public Integer getSoruCozmeMinutes() {
        return soruCozmeMinutes;
    }

    public void setSoruCozmeMinutes(Integer soruCozmeMinutes) {
        this.soruCozmeMinutes = soruCozmeMinutes;
    }

    public Integer getPomodoroMinutes() {
        return pomodoroMinutes;
    }

    public void setPomodoroMinutes(Integer pomodoroMinutes) {
        this.pomodoroMinutes = pomodoroMinutes;
    }

    public Integer getSoruCozmeSessions() {
        return soruCozmeSessions;
    }

    public void setSoruCozmeSessions(Integer soruCozmeSessions) {
        this.soruCozmeSessions = soruCozmeSessions;
    }

    public Integer getPomodoroSessions() {
        return pomodoroSessions;
    }

    public void setPomodoroSessions(Integer pomodoroSessions) {
        this.pomodoroSessions = pomodoroSessions;
    }
}








