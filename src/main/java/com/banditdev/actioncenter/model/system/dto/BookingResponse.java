package com.banditdev.actioncenter.model.system.dto;

import com.banditdev.actioncenter.model.system.Booking;
import com.banditdev.actioncenter.model.system.Session;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record BookingResponse(Long id, String nameOfCustomer,
                              String phoneNumber, String emailOfCustomer,
                              List<Long> sessionIds, LocalDate date, double totalPrice) {

    public static BookingResponse from(Booking booking) {
        List<Long> sessionIds = new ArrayList<>();
        for (Session session : booking.getSessions()) {
            sessionIds.add(session.getId());
        }

        return new BookingResponse(booking.getId(), booking.getNameOfCustomer(),
                booking.getPhoneNumber(), booking.getEmailOfCustomer(),
                sessionIds, booking.getDate(), booking.getTotalPrice());
    }
}
