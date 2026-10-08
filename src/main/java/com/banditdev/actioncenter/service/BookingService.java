package com.banditdev.actioncenter.service;


import com.banditdev.actioncenter.model.system.Activity;
import com.banditdev.actioncenter.model.system.Booking;
import com.banditdev.actioncenter.model.system.Session;
import com.banditdev.actioncenter.model.system.dto.BookingRequest;
import com.banditdev.actioncenter.model.system.dto.BookingUpdateRequest;
import com.banditdev.actioncenter.model.system.dto.BookingResponse;
import com.banditdev.actioncenter.model.system.dto.SessionDTO;
import com.banditdev.actioncenter.repository.ActivityRepository;
import com.banditdev.actioncenter.repository.BookingRepository;
import com.banditdev.actioncenter.repository.EquipmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final ActivityRepository activityRepository;
    private final EquipmentRepository equipmentRepository;
    private final SessionService sessionService;
    public BookingService(BookingRepository bookingRepository, ActivityRepository activityRepository,
                          EquipmentRepository equipmentRepository, SessionService sessionService) {
        this.bookingRepository = bookingRepository;
        this.activityRepository = activityRepository;
        this.equipmentRepository = equipmentRepository;
        this.sessionService = sessionService;
    }


    public BookingResponse createBooking(BookingRequest bookingRequest) {
        if (bookingRequest == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error: Empty booking request.");
        }

        if (bookingRequest.sessions() == null || bookingRequest.sessions().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error: A booking needs atleast one session");
        }

        List<SessionDTO> requestedSessions = bookingRequest.sessions();

        for (int i = 0; i < requestedSessions.size(); i++) {
            SessionDTO currentSession = requestedSessions.get(i);

            // Checks against sessions already saved in the database
            sessionService.assertAvailable(currentSession, null);

            // Checks against the other sessions in this same request
            for (int j = i + 1; j < requestedSessions.size(); j++) {
                SessionDTO otherSession = requestedSessions.get(j);

                boolean sameActivityAndDate =
                        currentSession.activityId().equals(otherSession.activityId())
                                && currentSession.dateOfActivity().equals(otherSession.dateOfActivity());

                boolean timesOverlap =
                        currentSession.startOfSession().isBefore(otherSession.endOfSession())
                                && currentSession.endOfSession().isAfter(otherSession.startOfSession());

                if (sameActivityAndDate && timesOverlap) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "To sessioner i samme booking overlapper.");
                }
            }
        }

        Booking booking = new Booking();
        booking.setNameOfCustomer(bookingRequest.nameOfCustomer());
        booking.setPhoneNumber(bookingRequest.phoneNumber());
        booking.setEmailOfCustomer(bookingRequest.emailOfCustomer());

        double totalPrice = 0;

        for (SessionDTO sessionDTO : bookingRequest.sessions()) {
            Session session = sessionService.createSession(sessionDTO, booking);
            booking.getSessions().add(session);

            totalPrice += session.getActivity().getPricePerActivity()
                    + session.getActivity().getPricePerPerson() * session.getAmountOfCustomers();
        }

        booking.setDate(booking.getSessions().getFirst().getDateOfActivity());
        booking.setTotalPrice(totalPrice);

        Booking saved = bookingRepository.save(booking);
        return BookingResponse.from(saved);
    }


    public Booking createBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    public List<BookingResponse> getAllBookings() {
        List<Booking> bookingList = bookingRepository.findAll();

        List<BookingResponse> bookings = new ArrayList<>();

        for (Booking booking : bookingList) {
            bookings.add(BookingResponse.from(booking));
        }
        return bookings;
    }

    @Transactional
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Booking not found... BookingId: " + id));
        return BookingResponse.from(booking);
    }

    // Rediger kundeoplysninger
    @Transactional
    public BookingResponse updateBooking(Long id, BookingUpdateRequest request) {
        Booking booking = bookingRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (request.nameOfCustomer() == null || request.nameOfCustomer().isBlank()
                || request.phoneNumber() == null
                || !request.phoneNumber().matches("\\+?[0-9]{1,15}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name and phone are required");
        }

        booking.setNameOfCustomer(request.nameOfCustomer().trim());
        booking.setPhoneNumber(request.phoneNumber());
        booking.setEmailOfCustomer(request.emailOfCustomer());

        return BookingResponse.from(bookingRepository.save(booking));
    }

    @Transactional
    public void deleteBookingById(Long id) {
        if (bookingRepository.findById(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        bookingRepository.deleteById(id);
    }
}

