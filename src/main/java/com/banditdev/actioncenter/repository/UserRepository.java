package com.banditdev.actioncenter.repository;

import com.banditdev.actioncenter.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
