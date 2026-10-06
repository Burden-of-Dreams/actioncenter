package com.banditdev.actioncenter.model.system.dto;

import java.time.LocalDate;
import java.util.List;

public record BookingDTO(Long id, String nameOfCustomer, String phoneNumber,
                         String emailOfCustomer, List<Long> sessionIds,
                         LocalDate date, double totalPrice) {

}