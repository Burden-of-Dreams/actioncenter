package com.banditdev.actioncenter.controller;

import com.banditdev.actioncenter.model.system.dto.EquipmentDTO;
import com.banditdev.actioncenter.service.EquipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping
    public ResponseEntity<List<EquipmentDTO>> getAllEquipment() {
        return ResponseEntity.ok(equipmentService.getAllEquipment());
    }
}