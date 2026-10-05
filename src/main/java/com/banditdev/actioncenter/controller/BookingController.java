package com.banditdev.actioncenter.controller;


import com.banditdev.actioncenter.model.system.Booking;
import com.banditdev.actioncenter.model.system.dto.BookingDTO;
import com.banditdev.actioncenter.service.BookingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }


    @GetMapping("/{bookingId}/bookings")
    public Booking getBooking(@PathVariable Long bookingId) {
        return bookingService.getBookingById(bookingId);
    }

    @GetMapping("/bookings/")
    public List<Booking> getBookings() {
        return bookingService.getBookings();
    }
}
