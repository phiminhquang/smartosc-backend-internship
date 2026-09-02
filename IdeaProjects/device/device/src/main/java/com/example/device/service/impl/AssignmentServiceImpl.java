package com.example.device.service.impl;

import com.example.device.dto.request.DeviceAssignmentRequest;
import com.example.device.dto.request.ReturnDeviceRequest;
import com.example.device.dto.response.AssignmentResponse;
import com.example.device.dto.response.DeviceAssignmentResponse;
import com.example.device.enums.DeviceAssignmentStatus;
import com.example.device.enums.DeviceReturnCondition;
import com.example.device.enums.DeviceState;
import com.example.device.exception.AppException;
import com.example.device.exception.ErrorCode;
import com.example.device.mapper.DeviceAssignmentMapper;
import com.example.device.model.Device;
import com.example.device.model.DeviceAssignment;
import com.example.device.model.User;
import com.example.device.repository.DeviceAssignmentRepository;
import com.example.device.repository.DeviceRepository;
import com.example.device.repository.UserRepository;
import com.example.device.service.AssignmentService;
import com.example.device.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private static final List<DeviceAssignmentStatus> OPEN_STATUSES =
            List.of(DeviceAssignmentStatus.ACTIVE, DeviceAssignmentStatus.OVERDUE);

    private final DeviceAssignmentRepository deviceAssignmentRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceAssignmentMapper deviceAssignmentMapper;
    private final EmailService emailService;

    @Value("${app.scheduler.reminder-hours}")
    private long reminderHours;

    @Override
    @Transactional
    public DeviceAssignmentResponse assignDevice(DeviceAssignmentRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND));

        boolean hasOpenAssignment = deviceAssignmentRepository
                .existsByDeviceIdAndStatusIn(device.getId(), OPEN_STATUSES);

        if (device.getState() != DeviceState.AVAILABLE || hasOpenAssignment) {
            throw new AppException(ErrorCode.DEVICE_NOT_AVAILABLE);
        }

        LocalDateTime now = LocalDateTime.now();
        String currentUserEmail = getCurrentUserEmail();

        DeviceAssignment assignment = DeviceAssignment.builder()
                .user(user)
                .device(device)
                .assignedBy(currentUserEmail)
                .assignedAt(now)
                .expectedReturnAt(request.getExpectedReturnAt())
                .status(DeviceAssignmentStatus.ACTIVE)
                .build();

        device.setState(DeviceState.ASSIGNED);
        device.setUpdatedBy(currentUserEmail);
        device.setUpdatedTime(now);

        deviceRepository.save(device);
        DeviceAssignment savedAssignment = deviceAssignmentRepository.save(assignment);

        return deviceAssignmentMapper.toResponse(savedAssignment);
    }

    @Override
    @Transactional
    public DeviceAssignmentResponse returnDevice(UUID assignmentId, ReturnDeviceRequest request) {
        DeviceAssignment assignment = deviceAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        if (assignment.getStatus() == DeviceAssignmentStatus.RETURNED) {
            throw new AppException(ErrorCode.DEVICE_ALREADY_RETURNED);
        }

        if (request.getCondition() == DeviceReturnCondition.DAMAGED
                && (request.getNote() == null || request.getNote().isBlank())) {
            throw new AppException(ErrorCode.RETURN_NOTE_REQUIRED);
        }

        LocalDateTime now = LocalDateTime.now();
        Device device = assignment.getDevice();

        deviceAssignmentMapper.updateReturn(request, assignment);

        assignment.setStatus(DeviceAssignmentStatus.RETURNED);
        assignment.setReturnedAt(now);

        device.setState(request.getCondition() == DeviceReturnCondition.GOOD
                ? DeviceState.AVAILABLE
                : DeviceState.UNDER_REPAIR);

        device.setUpdatedBy(getCurrentUserEmail());
        device.setUpdatedTime(now);

        return deviceAssignmentMapper.toResponse(assignment);
    }

    @Override
    public int sendDailyOverdueSummary() {
        List<DeviceAssignment> assignments = deviceAssignmentRepository
                .findByStatusOrderByExpectedReturnAtAsc(DeviceAssignmentStatus.OVERDUE);

        if (assignments.isEmpty()) {
            return 0;
        }

        List<User> recipients = userRepository
                .findDistinctByRoles_NameIn(List.of("ADMIN", "IT_STAFF"));

        if (recipients.isEmpty()) {
            return 0;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        StringBuilder content = new StringBuilder();
        content.append("DANH SÁCH THIẾT BỊ QUÁ HẠN\n\n");
        content.append("Tổng số thiết bị quá hạn: ").append(assignments.size()).append("\n\n");

        int index = 1;

        for (DeviceAssignment assignment : assignments) {
            content.append(index++).append(". ")
                    .append(assignment.getDevice().getName()).append("\n")
                    .append("Serial: ").append(assignment.getDevice().getSerialNumber()).append("\n")
                    .append("Người giữ: ").append(assignment.getUser().getName()).append("\n")
                    .append("Email: ").append(assignment.getUser().getEmail()).append("\n")
                    .append("Hạn trả: ").append(assignment.getExpectedReturnAt().format(formatter))
                    .append("\n\n");
        }

        String subject = "Báo cáo thiết bị quá hạn - "
                + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        int sent = 0;

        for (User recipient : recipients) {
            try {
                emailService.sendEmail(recipient.getEmail(), subject, content.toString());
                sent++;
            } catch (Exception e) {
                System.out.println("Không gửi được báo cáo quá hạn tới "
                        + recipient.getEmail() + ": " + e.getMessage());
            }
        }

        return sent;
    }

    @Override
    public DeviceAssignmentResponse getAssignment(UUID assignmentId) {
        DeviceAssignment assignment = deviceAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        return deviceAssignmentMapper.toResponse(assignment);
    }

    @Override
    public List<DeviceAssignmentResponse> getAssignments() {
        return deviceAssignmentRepository.findAllByOrderByAssignedAtDesc()
                .stream()
                .map(deviceAssignmentMapper::toResponse)
                .toList();
    }

    @Override
    public List<DeviceAssignmentResponse> getDevicesByUser(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return deviceAssignmentRepository.findByUserIdOrderByAssignedAtDesc(userId)
                .stream()
                .map(deviceAssignmentMapper::toResponse)
                .toList();
    }

    @Override
    public List<DeviceAssignmentResponse> getAssignmentsByStatus(DeviceAssignmentStatus status) {
        return deviceAssignmentRepository.findByStatusOrderByAssignedAtDesc(status)
                .stream()
                .map(deviceAssignmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public int updateOverdueAssignments() {
        List<DeviceAssignment> assignments = deviceAssignmentRepository
                .findByStatusAndExpectedReturnAtBefore(
                        DeviceAssignmentStatus.ACTIVE,
                        LocalDateTime.now()
                );

        assignments.forEach(a -> a.setStatus(DeviceAssignmentStatus.OVERDUE));
        return assignments.size();
    }

    @Override
    @Transactional
    public int sendOverdueNotifications() {
        List<DeviceAssignment> assignments = deviceAssignmentRepository
                .findByStatusAndOverdueNotifiedAtIsNull(DeviceAssignmentStatus.OVERDUE);

        int sent = 0;

        for (DeviceAssignment assignment : assignments) {
            try {
                User user = assignment.getUser();
                Device device = assignment.getDevice();

                String subject = "Thông báo thiết bị quá hạn";
                String content = "Xin chào " + user.getName() + ",\n\n"
                        + "Thiết bị " + device.getName() + " (" + device.getSerialNumber() + ") đã quá hạn trả.\n"
                        + "Hạn trả: " + assignment.getExpectedReturnAt() + "\n\n"
                        + "Vui lòng liên hệ bộ phận IT để hoàn trả thiết bị.";

                emailService.sendEmail(user.getEmail(), subject, content);
                assignment.setOverdueNotifiedAt(LocalDateTime.now());
                sent++;
            } catch (Exception e) {
                System.out.println("Không gửi được email tới " + assignment.getUser().getEmail() + ": " + e.getMessage());
            }
        }

        deviceAssignmentRepository.saveAll(assignments);
        return sent;
    }
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @Override
    @Transactional
    public int sendUpcomingDueNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusHours(reminderHours);

        List<DeviceAssignment> assignments = deviceAssignmentRepository
                .findByStatusAndReminderNotifiedAtIsNullAndExpectedReturnAtAfterAndExpectedReturnAtBefore(
                        DeviceAssignmentStatus.ACTIVE, now, deadline);

        int sent = 0;

        for (DeviceAssignment assignment : assignments) {
            try {
                User user = assignment.getUser();
                Device device = assignment.getDevice();

                String subject = "Nhắc nhở thiết bị sắp đến hạn trả";
                String content = "Xin chào " + user.getName() + ",\n\n"
                        + "Thiết bị " + device.getName() + " (" + device.getSerialNumber() + ") sắp đến hạn trả.\n"
                        + "Hạn trả: " + assignment.getExpectedReturnAt() + "\n\n"
                        + "Vui lòng hoàn trả thiết bị đúng hạn.";

                emailService.sendEmail(user.getEmail(), subject, content);
                assignment.setReminderNotifiedAt(LocalDateTime.now());
                sent++;
            } catch (Exception e) {
                System.out.println("Không gửi được email nhắc hạn tới "
                        + assignment.getUser().getEmail() + ": " + e.getMessage());
            }
        }

        deviceAssignmentRepository.saveAll(assignments);
        return sent;
    }

    @Override
    public List<DeviceAssignmentResponse> getMyAssignments() {
        return deviceAssignmentRepository
                .findByUser_EmailOrderByAssignedAtDesc(getCurrentUserEmail())
                .stream()
                .map(deviceAssignmentMapper::toResponse)
                .toList();
    }

}