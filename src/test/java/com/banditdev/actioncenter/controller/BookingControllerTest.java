package com.banditdev.actioncenter.controller;

import com.banditdev.actioncenter.model.system.dto.BookingRequest;
import com.banditdev.actioncenter.model.system.dto.BookingResponse;
import com.banditdev.actioncenter.service.BookingService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String VALID_BOOKING_JSON = """
            {
              "nameOfCustomer": "Anna Hansen",
              "phoneNumber": "12345678",
              "emailOfCustomer": "anna@example.com",
              "sessions": [
                {
                  "activityId": 1,
                  "amountOfCustomers": 4,
                  "equipmentIds": [5, 6],
                  "dateOfActivity": "2026-11-02",
                  "startOfSession": "09:00",
                  "endOfSession": "10:00"
                }
              ]
            }
            """;
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private BookingService bookingService;

    private BookingResponse sampleResponse() {
        LocalDate date = LocalDate.of(2026, 11, 2);
        BookingResponse.SessionDetails session = new BookingResponse.SessionDetails(
                10L, "Paintball", 4, date,
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                List.of("Marker (nr. 1)"), 1L, List.of(5L, 6L));

        return new BookingResponse(1L, "Anna Hansen", "12345678", "anna@example.com",
                List.of(10L), date, 700.0, List.of(session));
    }

    // ---------- POST /api/bookings ----------

    @Test
    void createBooking_validRequest_returns201AndBookingBody() throws Exception {
        when(bookingService.createBooking(any(BookingRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BOOKING_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nameOfCustomer").value("Anna Hansen"))
                .andExpect(jsonPath("$.date").value("2026-11-02"))
                .andExpect(jsonPath("$.totalPrice").value(700.0))
                .andExpect(jsonPath("$.sessionIds[0]").value(10))
                .andExpect(jsonPath("$.sessions[0].activityName").value("Paintball"))
                .andExpect(jsonPath("$.sessions[0].equipmentIds.length()").value(2));
    }

    @Test
    void createBooking_bindsJsonIntoBookingRequestCorrectly() throws Exception {
        when(bookingService.createBooking(any(BookingRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BOOKING_JSON))
                .andExpect(status().isCreated());

        ArgumentCaptor<BookingRequest> captor = ArgumentCaptor.forClass(BookingRequest.class);
        verify(bookingService).createBooking(captor.capture());
        BookingRequest sent = captor.getValue();

        assertEquals("Anna Hansen", sent.nameOfCustomer());
        assertEquals("12345678", sent.phoneNumber());
        assertEquals(1, sent.sessions().size());
        assertEquals(1L, sent.sessions().getFirst().activityId());
        assertEquals(4, sent.sessions().getFirst().amountOfCustomers());
        assertEquals(List.of(5L, 6L), sent.sessions().getFirst().equipmentIds());
        assertEquals(LocalDate.of(2026, 11, 2), sent.sessions().getFirst().dateOfActivity());
        assertEquals(LocalTime.of(9, 0), sent.sessions().getFirst().startOfSession());
    }

    @Test
    void createBooking_timeAlreadyBooked_returns409() throws Exception {
        when(bookingService.createBooking(any(BookingRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Tidspunktet er allerede booket."));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BOOKING_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void createBooking_outsideOpeningHours_returns400() throws Exception {
        when(bookingService.createBooking(any(BookingRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tidspunktet er uden for åbningstiden."));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BOOKING_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_noSessions_returns400() throws Exception {
        when(bookingService.createBooking(any(BookingRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error: A booking needs atleast one session"));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nameOfCustomer":"Anna","phoneNumber":"12345678","emailOfCustomer":"a@b.dk","sessions":[]}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_malformedJson_returns400AndNeverCallsService() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ this is not json"))
                .andExpect(status().isBadRequest());

        org.mockito.Mockito.verifyNoInteractions(bookingService);
    }

    @Test
    void createBooking_invalidDateFormat_returns400() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nameOfCustomer":"Anna","phoneNumber":"12345678","emailOfCustomer":"a@b.dk",
                                 "sessions":[{"activityId":1,"amountOfCustomers":2,"equipmentIds":[],
                                 "dateOfActivity":"not-a-date","startOfSession":"09:00","endOfSession":"10:00"}]}
                                """))
                .andExpect(status().isBadRequest());
    }

    // ---------- GET /api/bookings/{id} ----------

    @Test
    void getBooking_existing_returns200() throws Exception {
        when(bookingService.getBookingById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.phoneNumber").value("12345678"));
    }

    @Test
    void getBooking_notFound_returns404() throws Exception {
        when(bookingService.getBookingById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        mockMvc.perform(get("/api/bookings/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBooking_nonNumericId_returns400() throws Exception {
        mockMvc.perform(get("/api/bookings/abc"))
                .andExpect(status().isBadRequest());
    }

    // ---------- PATCH /api/bookings/{id} ----------

    @Test
    void updateBooking_validCustomerInfo_returns200() throws Exception {
        when(bookingService.updateBooking(eq(1L), any())).thenReturn(sampleResponse());

        mockMvc.perform(patch("/api/bookings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nameOfCustomer":"Anna Hansen","phoneNumber":"12345678","emailOfCustomer":"anna@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nameOfCustomer").value("Anna Hansen"));
    }

    @Test
    void updateBooking_invalidPhone_returns400() throws Exception {
        when(bookingService.updateBooking(eq(1L), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name and phone are required"));

        mockMvc.perform(patch("/api/bookings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nameOfCustomer":"Anna","phoneNumber":"abc","emailOfCustomer":"a@b.dk"}
                                """))
                .andExpect(status().isBadRequest());
    }

    // ---------- DELETE ----------

    @Test
    void deleteBooking_existing_returns204() throws Exception {
        doNothing().when(bookingService).deleteBookingById(1L);

        mockMvc.perform(delete("/api/bookings/delete/1"))
                .andExpect(status().isNoContent());

        verify(bookingService).deleteBookingById(1L);
    }

    @Test
    void deleteBooking_notFound_returns404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(bookingService).deleteBookingById(99L);

        mockMvc.perform(delete("/api/bookings/delete/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSession_lastSession_returns400() throws Exception {
        when(bookingService.deleteSession(1L, 10L))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Den sidste session kan ikke slettes. Slet hele bookingen i stedet."));

        mockMvc.perform(delete("/api/bookings/1/sessions/10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteSession_validSession_returns200WithUpdatedBooking() throws Exception {
        when(bookingService.deleteSession(1L, 10L)).thenReturn(sampleResponse());

        mockMvc.perform(delete("/api/bookings/1/sessions/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
