package com.banditdev.actioncenter.repository;

import com.banditdev.actioncenter.model.system.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByActivityEmployeesId(Long employeeID);

    @Query("""
        select s from Session s
        where s.activity.id = :activityId
          and s.dateOfActivity = :date
          and s.booking.id <> :excludeBookingId
        """)
    List<Session> findByActivityAndDate(@Param("activityId") Long activityId,
                                        @Param("date") LocalDate date,
                                        @Param("excludeBookingId") Long excludeBookingId);
}