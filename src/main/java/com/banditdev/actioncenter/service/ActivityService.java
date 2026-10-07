package com.banditdev.actioncenter.service;

import com.banditdev.actioncenter.model.system.Activity;
import com.banditdev.actioncenter.repository.ActivityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public Activity getEntityById(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Activity not found. Id: " + id));
    }
}