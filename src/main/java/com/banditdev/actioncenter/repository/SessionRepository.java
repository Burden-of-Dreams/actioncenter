package com.banditdev.actioncenter.repository;

import com.banditdev.actioncenter.model.system.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByTypeOfActivityEmployeesId(Long employeeID);
}
