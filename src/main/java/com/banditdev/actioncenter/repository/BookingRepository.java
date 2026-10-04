package com.banditdev.actioncenter.repository;

import com.banditdev.actioncenter.model.system.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
