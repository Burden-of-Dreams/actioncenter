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

    // Rediger session uden at ændre dens ID
    @Transactional
    public BookingResponse updateSession(Long bookingId, Long sessionId, SessionDTO request) {
        Booking booking = findBookingForUpdate(bookingId);
        Session session = findBookingSession(booking, sessionId);

        if (request.activityId() == null || request.dateOfActivity() == null
                || request.startOfSession() == null || request.amountOfCustomers() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Udfyld sessionens felter.");
        }

        Session updated = sessionService.createSession(request, booking);
        Activity activity = updated.getActivity();

        if (request.amountOfCustomers() > activity.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "For mange deltagere.");
        }

        java.time.LocalDateTime start = request.dateOfActivity().atTime(request.startOfSession());
        java.time.LocalDateTime end = start.plusMinutes(activity.getDurationMinutes());
        if (activity.getDurationMinutes() <= 0 || !start.toLocalDate().equals(end.toLocalDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sessionen skal slutte samme dag.");
        }

        // Udstyret skal høre til aktiviteten. Eksisterende reservationer bevares.
        for (var equipment : updated.getReservedEquipment()) {
            boolean belongsToActivity = activity.getEquipment().stream()
                    .anyMatch(item -> item.getId().equals(equipment.getId()));
            boolean alreadyReserved = session.getActivityId().equals(request.activityId())
                    && session.getReservedEquipmentIds().contains(equipment.getId());
            if (!belongsToActivity || (!alreadyReserved
                    && equipment.getStatus() != com.banditdev.actioncenter.model.system.Status.READY)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Udstyret kan ikke vælges.");
            }
        }

        session.setActivity(activity);
        session.setAmountOfCustomers(updated.getAmountOfCustomers());
        session.setDateOfActivity(updated.getDateOfActivity());
        session.setStartOfSession(updated.getStartOfSession());
        session.setEndOfSession(end.toLocalTime());
        session.setReservedEquipment(updated.getReservedEquipment());

        updateBookingTotals(booking);
        return BookingResponse.from(bookingRepository.saveAndFlush(booking));
    }

    // Slet session, men behold mindst én
    @Transactional
    public BookingResponse deleteSession(Long bookingId, Long sessionId) {
        Booking booking = findBookingForUpdate(bookingId);
        Session session = findBookingSession(booking, sessionId);
        if (booking.getSessions().size() <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Den sidste session kan ikke slettes. Slet hele bookingen i stedet.");
        }

        booking.getSessions().remove(session);
        updateBookingTotals(booking);
        return BookingResponse.from(bookingRepository.saveAndFlush(booking));
    }

    // Hent booking og kontrollér at sessionen tilhører den
    private Booking findBookingForUpdate(Long id) {
        return bookingRepository.findForUpdate(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    private Session findBookingSession(Booking booking, Long sessionId) {
        return booking.getSessions().stream()
                .filter(session -> session.getId().equals(sessionId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
    }

    // Opdater pris og dato efter en sessionændring
    private void updateBookingTotals(Booking booking) {
        double totalPrice = 0;
        for (Session session : booking.getSessions()) {
            totalPrice += session.getActivity().getPricePerActivity()
                    + session.getActivity().getPricePerPerson() * session.getAmountOfCustomers();
        }
        booking.setTotalPrice(totalPrice);
        booking.setDate(booking.getSessions().getFirst().getDateOfActivity());
    }
}

