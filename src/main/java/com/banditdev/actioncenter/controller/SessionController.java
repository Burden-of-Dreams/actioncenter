package com.banditdev.actioncenter.controller;

import com.banditdev.actioncenter.model.system.dto.EmployeeSessionDTO;
import com.banditdev.actioncenter.service.SessionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/employee/{employeeId}")
    public List<EmployeeSessionDTO> getEmployeeSessions(
            @PathVariable Long employeeId) {

        return sessionService.getSessionsForEmployee(employeeId);
    }
}