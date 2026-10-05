package com.banditdev.actioncenter.model.system.dto;

import com.banditdev.actioncenter.model.system.Session;

import java.time.LocalDate;
import java.util.List;

public record BookingRequest(String nameOfCustomer,
    String phoneNumber,
    String emailOfCustomer,
    List<Session> sessions,
    LocalDate date,
    double totalPrice) {
}
