package com.banditdev.actioncenter.repository;

import com.banditdev.actioncenter.model.system.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
}
