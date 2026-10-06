package com.banditdev.actioncenter.model.system.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record SessionDTO(Long id, Long activityId, int amountOfCustomers, List<Long> equipmentIds,
                         LocalDate dateOfActivity, LocalTime startOfSession, LocalTime endOfSession,
                         Long bookingId) {
}
