package com.banditdev.actioncenter.model.system.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class EmployeeSessionDTO {

    private Long sessionId;
    private String activityName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    public EmployeeSessionDTO(
            Long sessionId,
            String activityName,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime) {

        this.sessionId = sessionId;
        this.activityName = activityName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getActivityName() {
        return activityName;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}
