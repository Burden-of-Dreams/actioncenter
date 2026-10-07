package com.banditdev.actioncenter.service;


import com.banditdev.actioncenter.model.system.Equipment;
import com.banditdev.actioncenter.repository.EquipmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    public List<Equipment> getEntitiesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<Equipment> equipment = equipmentRepository.findAllById(ids);
        if (equipment.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "One or more equipment ids do not exist.");
        }
        return equipment;
    }
}
