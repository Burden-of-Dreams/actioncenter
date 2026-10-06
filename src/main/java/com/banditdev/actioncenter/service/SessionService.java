package com.banditdev.actioncenter.service;

import com.banditdev.actioncenter.model.system.Session;
import com.banditdev.actioncenter.model.system.dto.EmployeeSessionDTO;
import com.banditdev.actioncenter.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public List<EmployeeSessionDTO> getSessionsForEmployee(Long employeeId) {

        List<Session> sessions =
                sessionRepository.findByActivityEmployeesId(employeeId);

        return sessions.stream()
                .map(session -> new EmployeeSessionDTO(
                        session.getId(),
                        session.getActivity().getName(),
                        session.getDateOfActivity(),
                        session.getStartOfSession(),
                        session.getEndOfSession()
                ))
                .toList();
    }
}
