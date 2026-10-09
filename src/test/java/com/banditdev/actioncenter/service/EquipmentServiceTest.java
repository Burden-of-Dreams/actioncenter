package com.banditdev.actioncenter.service;

import com.banditdev.actioncenter.model.system.Activity;
import com.banditdev.actioncenter.model.system.Equipment;
import com.banditdev.actioncenter.model.system.Session;
import com.banditdev.actioncenter.model.system.Status;
import com.banditdev.actioncenter.model.system.dto.EquipmentDTO;
import com.banditdev.actioncenter.repository.EquipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock EquipmentRepository equipmentRepository;
    @InjectMocks EquipmentService equipmentService;

    @Test
    void getEntitiesByIds_unknownId_rejected() {
        when(equipmentRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(new Equipment()));

        var ex = assertThrows(ResponseStatusException.class,
                () -> equipmentService.getEntitiesByIds(List.of(1L, 2L)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void getAllEquipment_mapsEntityToDto() {
        Activity activity = new Activity();
        ReflectionTestUtils.setField(activity, "id", 3L);
        Session session = new Session();
        ReflectionTestUtils.setField(session, "id", 4L);
        Equipment kayak = new Equipment(12, "Kajak", List.of(activity), List.of(session), Status.READY);
        ReflectionTestUtils.setField(kayak, "id", 9L);
        when(equipmentRepository.findAll()).thenReturn(List.of(kayak));

        List<EquipmentDTO> result = equipmentService.getAllEquipment();

        assertEquals(List.of(new EquipmentDTO(9L, 12, "Kajak", List.of(3L), List.of(4L), Status.READY)), result);
    }
}
