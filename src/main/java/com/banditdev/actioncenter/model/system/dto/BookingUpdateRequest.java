package com.banditdev.actioncenter.model.system.dto;

// Kundeoplysninger der kan redigeres
public record BookingUpdateRequest(String nameOfCustomer,
                                   String phoneNumber,
                                   String emailOfCustomer) {
}

