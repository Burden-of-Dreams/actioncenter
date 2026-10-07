package com.banditdev.actioncenter.service;


import com.banditdev.actioncenter.model.system.Activity;
import com.banditdev.actioncenter.model.system.Equipment;
import com.banditdev.actioncenter.model.system.Session;
import com.banditdev.actioncenter.model.system.dto.EquipmentDTO;
import com.banditdev.actioncenter.repository.EquipmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    @Transactional
    public List<EquipmentDTO> getAllEquipment() {
        List<EquipmentDTO> results = new ArrayList<>();

        for (Equipment equipment : equipmentRepository.findAll()) {
            List<Long> activityIds = new ArrayList<>();

            for (Activity activity : equipment.getActivity()) {
                activityIds.add(activity.getId());
            }

            List<Long> sessionIds = new ArrayList<>();
            for (Session session : equipment.getSessions()) {
                sessionIds.add(session.getId());
            }

            EquipmentDTO dto = new EquipmentDTO(
                    equipment.getId(),
                    equipment.getNumber(),
                    equipment.getName(),
                    activityIds,
                    sessionIds,
                    equipment.getStatus()
            );

            results.add(dto);
        }

        return results;
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
