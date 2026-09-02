package com.example.device.service.impl;

import com.example.device.dto.request.DeviceCreationRequest;
import com.example.device.dto.request.DeviceStatusUpdateRequest;
import com.example.device.dto.request.DeviceUpdateRequest;
import com.example.device.dto.response.DeviceResponse;
import com.example.device.enums.DeviceAssignmentStatus;
import com.example.device.enums.DeviceCategory;
import com.example.device.enums.DeviceState;
import com.example.device.enums.RepairStatus;
import com.example.device.exception.AppException;
import com.example.device.exception.ErrorCode;
import com.example.device.mapper.DeviceMapper;
import com.example.device.model.Device;
import com.example.device.repository.DeviceAssignmentRepository;
import com.example.device.repository.DeviceRepairRepository;
import com.example.device.repository.DeviceRepository;
import com.example.device.service.DeviceSerialNumberGenerator;
import com.example.device.service.DeviceService;
import com.example.device.specification.DeviceSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private static final List<DeviceAssignmentStatus> OPEN_STATUSES =
            List.of(DeviceAssignmentStatus.ACTIVE, DeviceAssignmentStatus.OVERDUE);

    private static final List<RepairStatus> OPEN_REPAIR_STATUSES =
            List.of(RepairStatus.PENDING, RepairStatus.IN_PROGRESS);

    private final DeviceRepository deviceRepository;
    private final DeviceAssignmentRepository deviceAssignmentRepository;
    private final DeviceRepairRepository deviceRepairRepository;
    private final DeviceMapper deviceMapper;
    private final DeviceSerialNumberGenerator serialNumberGenerator;

    @Override
    public DeviceResponse createDevice(DeviceCreationRequest request) {
        Device device = deviceMapper.toDevice(request);
        String serialNumber;

        do {
            serialNumber = serialNumberGenerator.genarate(request.getCategory());
        } while (deviceRepository.existsBySerialNumber(serialNumber));

        device.setSerialNumber(serialNumber);
        device.setState(DeviceState.AVAILABLE);
        device.setUpdatedBy(getCurrentUserEmail());
        device.setUpdatedTime(LocalDateTime.now());

        return deviceMapper.toDeviceResponse(deviceRepository.save(device));
    }

    @Override
    public List<DeviceResponse> getDevices() {
        return deviceRepository.findAll()
                .stream()
                .map(deviceMapper::toDeviceResponse)
                .toList();
    }

    @Override
    public DeviceResponse getDeviceById(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND));

        return deviceMapper.toDeviceResponse(device);
    }

    @Override
    public DeviceResponse updateDevice(UUID id, DeviceUpdateRequest request) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND));

        deviceMapper.updateDevice(request, device);
        device.setUpdatedBy(getCurrentUserEmail());
        device.setUpdatedTime(LocalDateTime.now());

        return deviceMapper.toDeviceResponse(deviceRepository.save(device));
    }

    @Override
    @Transactional
    public void deleteDevice(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND));

        if (deviceAssignmentRepository.existsByDeviceId(id)) {
            throw new AppException(ErrorCode.DEVICE_HAS_ASSIGNMENT_HISTORY);
        }

        if (deviceRepairRepository.existsByDeviceId(id)) {
            throw new AppException(ErrorCode.DEVICE_HAS_REPAIR_HISTORY);
        }

        deviceRepository.delete(device);
    }

    @Override
    @Transactional
    public DeviceResponse updateDeviceState(UUID id, DeviceStatusUpdateRequest request) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND));

        boolean hasOpenAssignment = deviceAssignmentRepository
                .existsByDeviceIdAndStatusIn(id, OPEN_STATUSES);

        boolean hasOpenRepair = deviceRepairRepository
                .existsByDeviceIdAndStatusIn(id, OPEN_REPAIR_STATUSES);

        if (request.getState() == DeviceState.ASSIGNED
                || device.getState() == DeviceState.ASSIGNED
                || hasOpenAssignment) {
            throw new AppException(ErrorCode.DEVICE_STATE_MANAGED_BY_ASSIGNMENT);
        }

        if (hasOpenRepair) {
            throw new AppException(ErrorCode.DEVICE_STATE_MANAGED_BY_REPAIR);
        }

        device.setState(request.getState());
        device.setUpdatedBy(getCurrentUserEmail());
        device.setUpdatedTime(LocalDateTime.now());

        return deviceMapper.toDeviceResponse(device);
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @Override
    public Page<DeviceResponse> searchDevices(
            String keyword,
            DeviceState state,
            DeviceCategory category,
            int page,
            int size) {

        Specification<Device> spec = Specification.allOf(
                DeviceSpecification.hasKeyword(keyword),
                DeviceSpecification.hasState(state),
                DeviceSpecification.hasCategory(category)
        );

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        return deviceRepository.findAll(spec, pageable)
                .map(deviceMapper::toDeviceResponse);
    }

}