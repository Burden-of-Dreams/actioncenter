package com.banditdev.actioncenter.service;

import com.banditdev.actioncenter.model.system.Activity;
import com.banditdev.actioncenter.model.system.Equipment;
import com.banditdev.actioncenter.model.system.dto.ActivityDTO;
import com.banditdev.actioncenter.model.user.User;
import com.banditdev.actioncenter.repository.ActivityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public Activity getActivityById(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Activity not found. Id: " + id));
    }

    @Transactional
    public List<ActivityDTO> getAllActivities() {
        List<ActivityDTO> results = new ArrayList<>();

        for (Activity activity : activityRepository.findAll()) {

            List<Long> employeeIds = new ArrayList<>();
            for (User employee : activity.getEmployees()) {
                employeeIds.add(employee.getId());
            }

            List<Long> equipmentIds = new ArrayList<>();
            for (Equipment equipment : activity.getEquipment()) {
                equipmentIds.add(equipment.getId());
            }

            ActivityDTO dto = new ActivityDTO(
                    activity.getId(),
                    activity.getName(),
                    activity.getDescription(),
                    employeeIds,
                    activity.getAgeLimit(),
                    activity.getCapacity(),
                    activity.getDurationMinutes(),
                    equipmentIds,
                    activity.getPricePerActivity(),
                    activity.getPricePerPerson()
            );

            results.add(dto);
        }

        return results;
    }
}