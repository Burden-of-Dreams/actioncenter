package com.banditdev.actioncenter.service;

import com.banditdev.actioncenter.model.system.Activity;
import com.banditdev.actioncenter.model.system.Booking;
import com.banditdev.actioncenter.model.system.Equipment;
import com.banditdev.actioncenter.model.system.Session;
import com.banditdev.actioncenter.model.system.dto.EmployeeSessionDTO;
import com.banditdev.actioncenter.model.system.dto.SessionDTO;
import com.banditdev.actioncenter.repository.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SessionService {

    private static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(16, 0);
    private static final int MINUTES_BETWEEN_START_TIMES = 15;

    private final SessionRepository sessionRepository;
    private final ActivityService activityService;
    private final EquipmentService equipmentService;

    public SessionService(SessionRepository sessionRepository, ActivityService activityService,
                          EquipmentService equipmentService) {
        this.sessionRepository = sessionRepository;
        this.activityService = activityService;
        this.equipmentService = equipmentService;
    }

    public List<EmployeeSessionDTO> getSessionsForEmployee(Long employeeId) {
        List<Session> sessions = sessionRepository.findByActivityEmployeesId(employeeId);

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
        Activity activity = activityService.getActivityById(sessionDTO.activityId());
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

    // -------------------------
    // AVAILABILITY
    // -------------------------

    private List<Session> getExistingSessions(Long activityId, LocalDate date, Long excludeBookingId) {
        Long bookingIdToIgnore = (excludeBookingId == null) ? -1L : excludeBookingId;
        return sessionRepository.findByActivityAndDate(activityId, date, bookingIdToIgnore);
    }

    // The ONLY place the overlap rule exists
    private boolean isTimeAvailable(LocalTime newStartTime, LocalTime newEndTime,
                                    List<Session> existingSessions) {
        for (Session existingSession : existingSessions) {
            boolean newStartsBeforeExistingEnds = newStartTime.isBefore(existingSession.getEndOfSession());
            boolean newEndsAfterExistingStarts = newEndTime.isAfter(existingSession.getStartOfSession());

            if (newStartsBeforeExistingEnds && newEndsAfterExistingStarts) {
                return false;
            }
        }
        return true;
    }

    // For the dropdown
    public List<String> getAvailableStartTimes(Long activityId, LocalDate date, Long excludeBookingId) {
        Activity activity = activityService.getActivityById(activityId);
        List<Session> existingSessions = getExistingSessions(activityId, date, excludeBookingId);

        List<String> availableStartTimes = new ArrayList<>();
        LocalTime possibleStartTime = OPENING_TIME;

        while (!possibleStartTime.plusMinutes(activity.getDurationMinutes()).isAfter(CLOSING_TIME)) {
            LocalTime possibleEndTime = possibleStartTime.plusMinutes(activity.getDurationMinutes());

            if (isTimeAvailable(possibleStartTime, possibleEndTime, existingSessions)) {
                availableStartTimes.add(possibleStartTime.toString());   // "08:00"
            }
            possibleStartTime = possibleStartTime.plusMinutes(MINUTES_BETWEEN_START_TIMES);
        }
        return availableStartTimes;
    }

    // For saving (create now, edit later)
    public void assertAvailable(SessionDTO sessionDTO, Long excludeBookingId) {
        Activity activity = activityService.getActivityById(sessionDTO.activityId());
        LocalTime startTime = sessionDTO.startOfSession();
        LocalTime endTime = startTime.plusMinutes(activity.getDurationMinutes());

        boolean endsAfterMidnight = endTime.isBefore(startTime);
        if (startTime.isBefore(OPENING_TIME) || endTime.isAfter(CLOSING_TIME) || endsAfterMidnight) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tidspunktet er uden for åbningstiden.");
        }

        List<Session> existingSessions =
                getExistingSessions(sessionDTO.activityId(), sessionDTO.dateOfActivity(), excludeBookingId);

        if (!isTimeAvailable(startTime, endTime, existingSessions)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tidspunktet er allerede booket.");
        }
    }
}