package com.banditdev.actioncenter.repository;

import com.banditdev.actioncenter.model.user.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
