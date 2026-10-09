package com.example.device.service;

import com.example.device.dto.request.RepairCompleteRequest;
import com.example.device.dto.request.RepairCreationRequest;
import com.example.device.dto.request.RepairUnrepairableRequest;
import com.example.device.dto.response.RepairResponse;
import com.example.device.dto.response.PageResult;
import com.example.device.enums.RepairStatus;

import java.util.UUID;

public interface DeviceRepairService {

    RepairResponse createRepair(RepairCreationRequest request);

    RepairResponse startRepair(UUID repairId);

    RepairResponse completeRepair(UUID repairId, RepairCompleteRequest request);

    RepairResponse markUnrepairable(UUID repairId, RepairUnrepairableRequest request);

    RepairResponse getRepair(UUID repairId);

    PageResult<RepairResponse> getRepairs(
            RepairStatus status,
            UUID deviceId,
            int page,
            int size,
            String sort
    );

    PageResult<RepairResponse> getRepairsByDevice(
            UUID deviceId,
            int page,
            int size,
            String sort
    );
}
