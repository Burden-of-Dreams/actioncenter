package com.banditdev.actioncenter.model.system.dto;

import java.util.List;

public record BookingRequest(String nameOfCustomer,
    String phoneNumber,
    String emailOfCustomer,
    List<SessionDTO> sessions) {
}
