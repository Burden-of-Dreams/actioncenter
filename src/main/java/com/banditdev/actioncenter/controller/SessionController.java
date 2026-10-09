package com.banditdev.actioncenter.controller;

import com.banditdev.actioncenter.model.system.dto.EmployeeSessionDTO;
import com.banditdev.actioncenter.service.SessionService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/employee/{employeeId}")
    public List<EmployeeSessionDTO> getEmployeeSessions(@PathVariable Long employeeId) {
        return sessionService.getSessionsForEmployee(employeeId);
    }

    @GetMapping("/available-starts")
    public List<String> getAvailableStartTimes(
            @RequestParam Long activityId,
            @RequestParam LocalDate date,
            @RequestParam(required = false) Long excludeBookingId) {

        return sessionService.getAvailableStartTimes(activityId, date, excludeBookingId);
    }
}