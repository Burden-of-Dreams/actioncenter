package com.banditdev.actioncenter.repository;

import com.banditdev.actioncenter.model.system.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByActivityEmployeesId(Long employeeID);
    @Query("""
            SELECT DISTINCT s FROM Session s
            JOIN s.reservedEquipment e
            WHERE e.id IN :equipmentIds
              AND s.dateOfActivity = :date
              AND s.startOfSession < :end
              AND s.endOfSession > :start
            """)
    List<Session> findConflictingSessions(@Param("equipmentIds") Collection<Long> equipmentIds,
                                          @Param("date") LocalDate date,
                                          @Param("start") LocalTime start,
                                          @Param("end") LocalTime end);
}

