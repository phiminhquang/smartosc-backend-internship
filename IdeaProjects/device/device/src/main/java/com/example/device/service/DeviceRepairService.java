package com.example.device.service;

import com.example.device.dto.request.RepairCompleteRequest;
import com.example.device.dto.request.RepairCreationRequest;
import com.example.device.dto.request.RepairUnrepairableRequest;
import com.example.device.dto.response.RepairResponse;

import java.util.List;
import java.util.UUID;

public interface DeviceRepairService {

    RepairResponse createRepair(RepairCreationRequest request);

    RepairResponse startRepair(UUID repairId);

    RepairResponse completeRepair(UUID repairId, RepairCompleteRequest request);

    RepairResponse markUnrepairable(UUID repairId, RepairUnrepairableRequest request);

    RepairResponse getRepair(UUID repairId);

    List<RepairResponse> getRepairs();

    List<RepairResponse> getRepairsByDevice(UUID deviceId);
}