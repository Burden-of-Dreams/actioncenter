package com.banditdev.actioncenter.service;


import com.banditdev.actioncenter.model.system.Activity;
import com.banditdev.actioncenter.model.system.Booking;
import com.banditdev.actioncenter.model.system.Session;
import com.banditdev.actioncenter.model.system.dto.BookingRequest;
import com.banditdev.actioncenter.model.system.dto.BookingResponse;
import com.banditdev.actioncenter.model.system.dto.SessionDTO;
import com.banditdev.actioncenter.repository.ActivityRepository;
import com.banditdev.actioncenter.repository.BookingRepository;
import com.banditdev.actioncenter.repository.EquipmentRepository;
import org.springframework.http.HttpStatus;
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
    public BookingService(BookingRepository bookingRepository, ActivityRepository activityRepository,
                          EquipmentRepository equipmentRepository) {
        this.bookingRepository = bookingRepository;
        this.activityRepository = activityRepository;
        this.equipmentRepository = equipmentRepository;
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


        booking.setDate(booking.getSessions().get(0).getDateOfActivity());
        booking.setTotalPrice(totalPrice);

        Booking saved = bookingRepository.save(booking);
        return BookingResponse.from(saved);
    }

    public Booking createBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    public List<Booking> getBookings() {
        return bookingRepository.findAll();
    }

    public List<BookingResponse> getAllBookings() {
        List<Booking> bookingList = bookingRepository.findAll();

        List<BookingResponse> bookings = new ArrayList<>();

        for (Booking booking : bookingList) {
            bookings.add(BookingResponse.from(booking));
        }
        return bookings;
    }

    public Booking getBookingById(Long id) {
        Optional<Booking> bookingOptional = bookingRepository.findById(id);
        if (bookingOptional.isEmpty()) {
            throw new RuntimeException("Booking not found. Id: " + id);
        }
        return bookingOptional.get();
    }

    @Transactional
    public void deleteBookingById(Long id) {
        if (bookingRepository.findById(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        bookingRepository.deleteById(id);
    }
}
