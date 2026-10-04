package com.banditdev.actioncenter.repository;

import com.banditdev.actioncenter.model.system.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
}