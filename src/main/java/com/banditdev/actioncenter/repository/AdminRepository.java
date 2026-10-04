package com.banditdev.actioncenter.repository;

import com.banditdev.actioncenter.model.user.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}
