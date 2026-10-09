package com.banditdev.actioncenter.repository;

import com.banditdev.actioncenter.model.system.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Lås bookingen mens dens sessions ændres
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id = :id")
    Optional<Booking> findForUpdate(@Param("id") Long id);
}

