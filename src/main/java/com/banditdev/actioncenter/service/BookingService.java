package com.banditdev.actioncenter.service;


import com.banditdev.actioncenter.model.system.Booking;
import com.banditdev.actioncenter.model.system.dto.BookingRequest;
import com.banditdev.actioncenter.model.system.dto.BookingResponse;
import com.banditdev.actioncenter.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }


    public BookingResponse createBooking(BookingRequest bookingRequest) {
        if (bookingRequest == null) {
            throw new IllegalArgumentException("Error: Empty booking request.");
        }

        Booking booking = new Booking();
        booking.setNameOfCustomer(bookingRequest.nameOfCustomer());
        booking.setPhoneNumber(bookingRequest.phoneNumber());
        booking.setEmailOfCustomer(bookingRequest.emailOfCustomer());
        booking.setSessions(bookingRequest.sessions());
        booking.setDate(bookingRequest.date());
        booking.setTotalPrice(bookingRequest.totalPrice());

        bookingRepository.save(booking);

        return BookingResponse.from(booking);

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

    public void deleteBookingById(Long id) {
        //TODO måske lav "Transactional" og evt. skal den også slette sessions under selve bookingen?
        bookingRepository.deleteById(id);
    }

}
