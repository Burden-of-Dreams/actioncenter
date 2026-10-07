package com.banditdev.actioncenter.service;

import com.banditdev.actioncenter.model.system.Activity;
import com.banditdev.actioncenter.model.system.Booking;
import com.banditdev.actioncenter.model.system.Equipment;
import com.banditdev.actioncenter.model.system.Session;
import com.banditdev.actioncenter.model.system.dto.EmployeeSessionDTO;
import com.banditdev.actioncenter.model.system.dto.SessionDTO;
import com.banditdev.actioncenter.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final ActivityService activityService;
    private final EquipmentService equipmentService;

    public SessionService(SessionRepository sessionRepository, ActivityService activityService, EquipmentService equipmentService) {
        this.sessionRepository = sessionRepository;
        this.activityService = activityService;
        this.equipmentService = equipmentService;
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

    public Session createSession(SessionDTO sessionDTO, Booking booking) {
        Activity activity = activityService.getEntityById(sessionDTO.activityId());
        List<Equipment> equipment = equipmentService.getEntitiesByIds(sessionDTO.equipmentIds());

        Session session = new Session();
        session.setActivity(activity);
        session.setAmountOfCustomers(sessionDTO.amountOfCustomers());
        session.setReservedEquipment(equipment);
        session.setDateOfActivity(sessionDTO.dateOfActivity());
        session.setStartOfSession(sessionDTO.startOfSession());
        session.setEndOfSession(sessionDTO.endOfSession());
        session.setBooking(booking);
        return session;
    }
}
