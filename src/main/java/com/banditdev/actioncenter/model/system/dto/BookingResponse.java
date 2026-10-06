package com.banditdev.actioncenter.model.system.dto;

import com.banditdev.actioncenter.model.system.Booking;
import com.banditdev.actioncenter.model.system.Session;

import java.time.LocalDate;
import java.util.List;

public record BookingResponse(Long id, String nameOfCustomer,
                              String phoneNumber, String emailOfCustomer,
                              List<Session> sessions, LocalDate date, double totalPrice) {


    public static BookingResponse from(Booking booking) {
        return new BookingResponse(booking.getId(), booking.getNameOfCustomer(), booking.getPhoneNumber(),
                booking.getEmailOfCustomer(), booking.getSessions(), booking.getDate(), booking.getTotalPrice());
    }
}
