package com.banditdev.actioncenter.service;

import com.banditdev.actioncenter.model.system.Activity;
import com.banditdev.actioncenter.model.system.Booking;
import com.banditdev.actioncenter.model.system.Session;
import com.banditdev.actioncenter.model.system.dto.BookingRequest;
import com.banditdev.actioncenter.model.system.dto.BookingResponse;
import com.banditdev.actioncenter.model.system.dto.BookingUpdateRequest;
import com.banditdev.actioncenter.model.system.dto.SessionDTO;
import com.banditdev.actioncenter.repository.ActivityRepository;
import com.banditdev.actioncenter.repository.BookingRepository;
import com.banditdev.actioncenter.repository.EquipmentRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock BookingRepository bookingRepository;
    @Mock ActivityRepository activityRepository;
    @Mock EquipmentRepository equipmentRepository;
    @Mock SessionService sessionService;
    @InjectMocks BookingService bookingService;

    private static final LocalDate DAY_ONE = LocalDate.of(2026, 10, 15);
    private static final LocalDate DAY_TWO = LocalDate.of(2026, 10, 16);

    @Test
    void createBooking_savesBookingWithTotalPriceAndFirstSessionDate() {
        Activity activity = activity(1L, 500, 100);
        SessionDTO first = sessionDto(1L, DAY_ONE, LocalTime.of(10, 0), LocalTime.of(11, 0), 4);
        SessionDTO second = sessionDto(1L, DAY_TWO, LocalTime.of(10, 0), LocalTime.of(11, 0), 2);
        givenSessionServiceBuilds(first, activity);
        givenSessionServiceBuilds(second, activity);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.createBooking(
                new BookingRequest("Anna", "12345678", "anna@mail.dk", List.of(first, second)));

        assertEquals(1600, response.totalPrice());   // (500 + 100*4) + (500 + 100*2)
        assertEquals(DAY_ONE, response.date());
        assertEquals(2, response.sessions().size());
    }

    @Test
    void createBooking_overlappingSessionsInSameRequest_rejectedAndNotSaved() {
        SessionDTO first = sessionDto(1L, DAY_ONE, LocalTime.of(10, 0), LocalTime.of(11, 0), 4);
        SessionDTO second = sessionDto(1L, DAY_ONE, LocalTime.of(10, 30), LocalTime.of(11, 30), 2);

        var ex = assertThrows(ResponseStatusException.class, () -> bookingService.createBooking(
                new BookingRequest("Anna", "12345678", "anna@mail.dk", List.of(first, second))));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void updateBooking_savesNewCustomerInfo() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(new Booking()));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.updateBooking(1L,
                new BookingUpdateRequest("Anna Hansen", "+4512345678", "anna@mail.dk"));

        assertEquals("Anna Hansen", response.nameOfCustomer());
        assertEquals("+4512345678", response.phoneNumber());
        assertEquals("anna@mail.dk", response.emailOfCustomer());
    }

    @Test
    void deleteSession_lastSession_rejected() {
        Session onlySession = new Session();
        ReflectionTestUtils.setField(onlySession, "id", 5L);
        Booking booking = new Booking();
        booking.getSessions().add(onlySession);
        when(bookingRepository.findForUpdate(1L)).thenReturn(Optional.of(booking));

        var ex = assertThrows(ResponseStatusException.class, () -> bookingService.deleteSession(1L, 5L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(bookingRepository, never()).saveAndFlush(any());
    }

    private void givenSessionServiceBuilds(SessionDTO dto, Activity activity) {
        when(sessionService.createSession(eq(dto), any(Booking.class))).thenAnswer(inv -> {
            Session session = new Session();
            session.setActivity(activity);
            session.setAmountOfCustomers(dto.amountOfCustomers());
            session.setDateOfActivity(dto.dateOfActivity());
            session.setStartOfSession(dto.startOfSession());
            session.setEndOfSession(dto.endOfSession());
            return session;
        });
    }

    private static Activity activity(Long id, double pricePerActivity, double pricePerPerson) {
        Activity activity = new Activity();
        ReflectionTestUtils.setField(activity, "id", id);
        activity.setPricePerActivity(pricePerActivity);
        activity.setPricePerPerson(pricePerPerson);
        return activity;
    }

    private static SessionDTO sessionDto(Long activityId, LocalDate date, LocalTime start, LocalTime end,
                                         int customers) {
        return new SessionDTO(null, activityId, customers, List.of(), date, start, end, null);
    }
}
