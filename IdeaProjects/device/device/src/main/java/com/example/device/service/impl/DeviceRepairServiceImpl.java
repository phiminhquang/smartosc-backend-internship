package com.example.device.service.impl;

import com.example.device.dto.request.RepairCompleteRequest;
import com.example.device.dto.request.RepairCreationRequest;
import com.example.device.dto.request.RepairUnrepairableRequest;
import com.example.device.dto.response.RepairResponse;
import com.example.device.dto.response.PageResult;
import com.example.device.enums.DeviceState;
import com.example.device.enums.RepairStatus;
import com.example.device.exception.AppException;
import com.example.device.exception.ErrorCode;
import com.example.device.mapper.DeviceRepairMapper;
import com.example.device.model.Device;
import com.example.device.model.DeviceRepair;
import com.example.device.repository.DeviceRepairRepository;
import com.example.device.repository.DeviceRepository;
import com.example.device.service.DeviceRepairService;
import com.example.device.service.PaginationSupport;
import com.example.device.specification.DeviceRepairSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceRepairServiceImpl implements DeviceRepairService {

    private static final Map<String, String> REPAIR_SORTS = Map.of(
            "createdAt", "createdAt",
            "startedAt", "startedAt",
            "finishedAt", "finishedAt",
            "status", "status",
            "cost", "cost"
    );

    private static final List<RepairStatus> OPEN_STATUSES =
            List.of(RepairStatus.PENDING, RepairStatus.IN_PROGRESS);

    private final DeviceRepairRepository repairRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceRepairMapper repairMapper;

    @Override
    @Transactional
    public RepairResponse createRepair(RepairCreationRequest request) {
        Device device = deviceRepository.findByIdForUpdate(request.getDeviceId())
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND));

        if (device.getState() != DeviceState.UNDER_REPAIR) {
            throw new AppException(ErrorCode.DEVICE_NOT_UNDER_REPAIR);
        }

        if (repairRepository.existsByDeviceIdAndStatusIn(device.getId(), OPEN_STATUSES)) {
            throw new AppException(ErrorCode.REPAIR_ALREADY_OPEN);
        }

        DeviceRepair repair = repairMapper.toEntity(request);

        repair.setDevice(device);
        repair.setStatus(RepairStatus.PENDING);
        repair.setCreatedBy(getCurrentUserEmail());
        repair.setCreatedAt(LocalDateTime.now());

        return repairMapper.toResponse(repairRepository.save(repair));
    }

    @Override
    @Transactional
    public RepairResponse startRepair(UUID repairId) {
        DeviceRepair repair = getRepairForUpdate(repairId);

        if (repair.getStatus() != RepairStatus.PENDING) {
            throw new AppException(ErrorCode.REPAIR_CANNOT_START);
        }

        repair.setStatus(RepairStatus.IN_PROGRESS);
        repair.setStartedBy(getCurrentUserEmail());
        repair.setStartedAt(LocalDateTime.now());

        return repairMapper.toResponse(repair);
    }

    @Override
    @Transactional
    public RepairResponse completeRepair(UUID repairId, RepairCompleteRequest request) {
        DeviceRepair repair = getRepairForUpdate(repairId);

        if (repair.getStatus() != RepairStatus.IN_PROGRESS) {
            throw new AppException(ErrorCode.REPAIR_CANNOT_FINISH);
        }

        LocalDateTime now = LocalDateTime.now();
        String currentUser = getCurrentUserEmail();

        repairMapper.updateComplete(request, repair);

        repair.setStatus(RepairStatus.COMPLETED);
        repair.setFinishedBy(currentUser);
        repair.setFinishedAt(now);

        Device device = repair.getDevice();
        device.setState(DeviceState.AVAILABLE);
        device.setUpdatedBy(currentUser);
        device.setUpdatedTime(now);

        return repairMapper.toResponse(repair);
    }

    @Override
    @Transactional
    public RepairResponse markUnrepairable(UUID repairId, RepairUnrepairableRequest request) {
        DeviceRepair repair = getRepairForUpdate(repairId);

        if (repair.getStatus() != RepairStatus.IN_PROGRESS) {
            throw new AppException(ErrorCode.REPAIR_CANNOT_FINISH);
        }

        LocalDateTime now = LocalDateTime.now();
        String currentUser = getCurrentUserEmail();

        repairMapper.updateUnrepairable(request, repair);

        repair.setStatus(RepairStatus.UNREPAIRABLE);
        repair.setFinishedBy(currentUser);
        repair.setFinishedAt(now);

        Device device = repair.getDevice();
        device.setState(DeviceState.UNDER_REPAIR);
        device.setUpdatedBy(currentUser);
        device.setUpdatedTime(now);

        return repairMapper.toResponse(repair);
    }

    @Override
    @Transactional(readOnly = true)
    public RepairResponse getRepair(UUID repairId) {
        return repairMapper.toResponse(getRepairEntity(repairId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<RepairResponse> getRepairs(
            RepairStatus status,
            UUID deviceId,
            int page,
            int size,
            String sort
    ) {
        Specification<DeviceRepair> specification = Specification.allOf(
                DeviceRepairSpecification.hasStatus(status),
                DeviceRepairSpecification.hasDeviceId(deviceId)
        );
        return findRepairs(specification, page, size, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<RepairResponse> getRepairsByDevice(
            UUID deviceId,
            int page,
            int size,
            String sort
    ) {
        if (!deviceRepository.existsById(deviceId)) {
            throw new AppException(ErrorCode.DEVICE_NOT_FOUND);
        }

        return findRepairs(DeviceRepairSpecification.hasDeviceId(deviceId), page, size, sort);
    }

    private PageResult<RepairResponse> findRepairs(
            Specification<DeviceRepair> specification,
            int page,
            int size,
            String sort
    ) {
        Pageable pageable = PaginationSupport.pageRequest(
                page, size, sort, REPAIR_SORTS, "createdAt", Sort.Direction.DESC
        );
        return PageResult.from(repairRepository.findAll(specification, pageable)
                .map(repairMapper::toResponse));
    }

    private DeviceRepair getRepairEntity(UUID repairId) {
        return repairRepository.findByIdWithDevice(repairId)
                .orElseThrow(() -> new AppException(ErrorCode.REPAIR_NOT_FOUND));
    }

    private DeviceRepair getRepairForUpdate(UUID repairId) {
        return repairRepository.findByIdForUpdate(repairId)
                .orElseThrow(() -> new AppException(ErrorCode.REPAIR_NOT_FOUND));
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
