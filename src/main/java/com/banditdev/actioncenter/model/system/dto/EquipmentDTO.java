package com.banditdev.actioncenter.model.system.dto;

import com.banditdev.actioncenter.model.system.Status;

import java.util.List;

public record EquipmentDTO(Long id, int number, String name, List<Long> activityIds,
                            List<Long> sessionIds, Status status) {

}
