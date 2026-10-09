package com.banditdev.actioncenter.service;

import com.banditdev.actioncenter.model.system.Activity;
import com.banditdev.actioncenter.model.system.Session;
import com.banditdev.actioncenter.model.system.dto.EmployeeSessionDTO;
import com.banditdev.actioncenter.model.system.dto.SessionDTO;
import com.banditdev.actioncenter.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock SessionRepository sessionRepository;
    @Mock ActivityService activityService;
    @Mock EquipmentService equipmentService;
    @InjectMocks SessionService sessionService;

    private static final Long ACTIVITY_ID = 1L;
    private static final LocalDate DATE = LocalDate.of(2026, 10, 15);

    @Test
    void assertAvailable_rejectsOverlappingSession() {
        givenActivityWithDuration(60);
        givenExistingSessions(session(LocalTime.of(9, 0), LocalTime.of(10, 0)));

        var ex = assertThrows(ResponseStatusException.class,
                () -> sessionService.assertAvailable(requestStartingAt(LocalTime.of(9, 30)), null));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void assertAvailable_allowsSessionStartingExactlyWhenExistingEnds() {
        givenActivityWithDuration(60);
        givenExistingSessions(session(LocalTime.of(9, 0), LocalTime.of(10, 0)));

        assertDoesNotThrow(() -> sessionService.assertAvailable(requestStartingAt(LocalTime.of(10, 0)), null));
    }

    @Test
    void getAvailableStartTimes_existingSession_removesOnlyThatSlot() {
        givenActivityWithDuration(60);
        givenExistingSessions(session(LocalTime.of(10, 0), LocalTime.of(11, 0)));

        List<String> result = sessionService.getAvailableStartTimes(ACTIVITY_ID, DATE, null);

        assertEquals(List.of("08:00", "09:00", "11:00", "12:00", "13:00", "14:00", "15:00"), result);
    }

    @Test
    void getSessionsForEmployee_mapsSessionToDto() {
        Activity activity = new Activity();
        activity.setName("Paintball");
        Session session = session(LocalTime.of(10, 0), LocalTime.of(11, 0));
        session.setActivity(activity);
        session.setDateOfActivity(DATE);
        ReflectionTestUtils.setField(session, "id", 7L);
        when(sessionRepository.findByActivityEmployeesId(3L)).thenReturn(List.of(session));

        EmployeeSessionDTO dto = sessionService.getSessionsForEmployee(3L).getFirst();

        assertEquals(7L, dto.getSessionId());
        assertEquals("Paintball", dto.getActivityName());
        assertEquals(DATE, dto.getDate());
        assertEquals(LocalTime.of(10, 0), dto.getStartTime());
        assertEquals(LocalTime.of(11, 0), dto.getEndTime());
    }

    private void givenActivityWithDuration(int minutes) {
        Activity activity = new Activity();
        activity.setDurationMinutes(minutes);
        when(activityService.getActivityById(ACTIVITY_ID)).thenReturn(activity);
    }

    private void givenExistingSessions(Session... sessions) {
        when(sessionRepository.findByActivityAndDate(ACTIVITY_ID, DATE, -1L)).thenReturn(List.of(sessions));
    }

    private static Session session(LocalTime start, LocalTime end) {
        Session session = new Session();
        session.setStartOfSession(start);
        session.setEndOfSession(end);
        return session;
    }

    private static SessionDTO requestStartingAt(LocalTime start) {
        return new SessionDTO(null, ACTIVITY_ID, 2, List.of(), DATE, start, null, null);
    }
}
