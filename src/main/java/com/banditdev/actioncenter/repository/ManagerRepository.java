package com.banditdev.actioncenter.repository;

import com.banditdev.actioncenter.model.user.Manager;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagerRepository extends JpaRepository<Manager, Long> {
}
