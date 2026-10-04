package com.banditdev.actioncenter.repository;

import com.banditdev.actioncenter.model.system.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {
}
