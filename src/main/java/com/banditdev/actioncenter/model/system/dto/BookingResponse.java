package com.banditdev.actioncenter.model.system.dto;

import com.banditdev.actioncenter.model.system.Booking;
import com.banditdev.actioncenter.model.system.Session;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public record BookingResponse(Long id, String nameOfCustomer,
                              String phoneNumber, String emailOfCustomer,
                              List<Long> sessionIds, LocalDate date, double totalPrice,
                              List<SessionDetails> sessions) {

    public static BookingResponse from(Booking booking) {
        List<Long> sessionIds = new ArrayList<>();
        List<SessionDetails> sessions = new ArrayList<>();
        for (Session session : booking.getSessions()) {
            sessionIds.add(session.getId());
            sessions.add(SessionDetails.from(session));
        }

        return new BookingResponse(booking.getId(), booking.getNameOfCustomer(),
                booking.getPhoneNumber(), booking.getEmailOfCustomer(),
                sessionIds, booking.getDate(), booking.getTotalPrice(), sessions);
    }

    // Oplysninger til bookingens sessionliste
    public record SessionDetails(Long id, String activityName, int amountOfCustomers,
                                 LocalDate dateOfActivity, LocalTime startOfSession,
                                 LocalTime endOfSession, List<String> equipmentNames,
                                 Long activityId, List<Long> equipmentIds) {

        public static SessionDetails from(Session session) {
            List<String> equipmentNames = session.getReservedEquipment().stream()
                    .map(equipment -> equipment.getName() + " (nr. " + equipment.getNumber() + ")")
                    .toList();

            return new SessionDetails(
                    session.getId(),
                    session.getActivity().getName(),
                    session.getAmountOfCustomers(),
                    session.getDateOfActivity(),
                    session.getStartOfSession(),
                    session.getEndOfSession(),
                    equipmentNames,
                    session.getActivityId(),
                    session.getReservedEquipmentIds()
            );
        }
    }
}


