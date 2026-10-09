package com.banditdev.actioncenter.controller;

import com.banditdev.actioncenter.model.system.dto.BookingRequest;
import com.banditdev.actioncenter.model.system.dto.BookingUpdateRequest;
import com.banditdev.actioncenter.model.system.dto.BookingResponse;
import com.banditdev.actioncenter.model.system.dto.SessionDTO;
import com.banditdev.actioncenter.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }


    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest bookingRequest) {
        BookingResponse createdBooking = bookingService.createBooking(bookingRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
    }

    // Rediger kundeoplysninger
    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> updateBooking(
            @PathVariable Long bookingId,
            @RequestBody BookingUpdateRequest request) {
        return ResponseEntity.ok(bookingService.updateBooking(bookingId, request));
    }

    // Rediger en session på bookingen
    @PutMapping("/{bookingId}/sessions/{sessionId}")
    public ResponseEntity<BookingResponse> updateSession(
            @PathVariable Long bookingId, @PathVariable Long sessionId,
            @RequestBody SessionDTO request) {
        return ResponseEntity.ok(bookingService.updateSession(bookingId, sessionId, request));
    }

    // Slet en session fra bookingen
    @DeleteMapping("/{bookingId}/sessions/{sessionId}")
    public ResponseEntity<BookingResponse> deleteSession(
            @PathVariable Long bookingId, @PathVariable Long sessionId) {
        return ResponseEntity.ok(bookingService.deleteSession(bookingId, sessionId));
    }

    @DeleteMapping("/delete/{bookingId}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long bookingId) {
        bookingService.deleteBookingById(bookingId);
        return ResponseEntity.noContent().build();
    }
}


