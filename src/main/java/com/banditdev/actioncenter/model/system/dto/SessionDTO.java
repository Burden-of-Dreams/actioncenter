package com.banditdev.actioncenter.model.system.dto;

import com.banditdev.actioncenter.model.system.Session;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record SessionDTO(Long id, Long activityId, int amountOfCustomers, List<Long> equipmentIds,
                         LocalDate dateOfActivity, LocalTime startOfSession, LocalTime endOfSession,
                         Long bookingId) {


    public static SessionDTO from(Session session) {
        return new SessionDTO(session.getId(), session.getActivityId(), session.getAmountOfCustomers(),
                session.getReservedEquipmentIds(), session.getDateOfActivity(), session.getStartOfSession(),
                session.getEndOfSession(), session.getBooking().getId());
    }
}
