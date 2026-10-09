package com.banditdev.actioncenter.service;

import com.banditdev.actioncenter.model.system.Activity;
import com.banditdev.actioncenter.model.system.Equipment;
import com.banditdev.actioncenter.model.system.dto.ActivityDTO;
import com.banditdev.actioncenter.model.user.Role;
import com.banditdev.actioncenter.model.user.User;
import com.banditdev.actioncenter.repository.ActivityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock ActivityRepository activityRepository;
    @InjectMocks ActivityService activityService;

    @Test
    void getActivityById_unknownId_rejected() {
        when(activityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> activityService.getActivityById(99L));
    }

    @Test
    void getAllActivities_mapsEmployeeAndEquipmentIds() {
        User employee = new User("anna", "secret", Role.EMPLOYEE);
        ReflectionTestUtils.setField(employee, "id", 2L);
        Equipment kayak = new Equipment();
        ReflectionTestUtils.setField(kayak, "id", 3L);
        Activity activity = new Activity();
        ReflectionTestUtils.setField(activity, "id", 1L);
        activity.setName("Kajaktur");
        activity.setEmployees(List.of(employee));
        activity.setEquipment(List.of(kayak));
        when(activityRepository.findAll()).thenReturn(List.of(activity));

        ActivityDTO dto = activityService.getAllActivities().getFirst();

        assertEquals(1L, dto.getId());
        assertEquals("Kajaktur", dto.getName());
        assertEquals(List.of(2L), dto.getEmployeeIds());
        assertEquals(List.of(3L), dto.getEquipmentIds());
    }
}
