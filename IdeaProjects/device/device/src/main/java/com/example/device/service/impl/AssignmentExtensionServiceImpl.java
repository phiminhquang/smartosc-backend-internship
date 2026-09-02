package com.example.device.service.impl;

import com.example.device.dto.request.ExtensionRequestCreationRequest;
import com.example.device.dto.request.ExtensionReviewRequest;
import com.example.device.dto.response.ExtensionResponse;
import com.example.device.enums.DeviceAssignmentStatus;
import com.example.device.enums.ExtensionRequestStatus;
import com.example.device.exception.AppException;
import com.example.device.exception.ErrorCode;
import com.example.device.mapper.AssignmentExtensionMapper;
import com.example.device.model.AssignmentExtension;
import com.example.device.model.DeviceAssignment;
import com.example.device.model.User;
import com.example.device.repository.AssignmentExtensionRepository;
import com.example.device.repository.DeviceAssignmentRepository;
import com.example.device.repository.UserRepository;
import com.example.device.service.AssignmentExtensionService;
import com.example.device.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignmentExtensionServiceImpl implements AssignmentExtensionService {

    private final AssignmentExtensionRepository extensionRepository;
    private final DeviceAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AssignmentExtensionMapper extensionMapper;

    @Override
    @Transactional
    public ExtensionResponse createRequest(UUID assignmentId, ExtensionRequestCreationRequest request) {
        DeviceAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        User currentUser = getCurrentUser();

        if (assignment.getStatus() == DeviceAssignmentStatus.RETURNED) {
            throw new AppException(ErrorCode.CANNOT_EXTEND_RETURNED_ASSIGNMENT);
        }

        if (!assignment.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (!request.getRequestedReturnAt().isAfter(assignment.getExpectedReturnAt())) {
            throw new AppException(ErrorCode.INVALID_EXTENSION_DATE);
        }

        if (extensionRepository.existsByAssignmentIdAndStatus(
                assignmentId, ExtensionRequestStatus.PENDING)) {
            throw new AppException(ErrorCode.EXTENSION_REQUEST_ALREADY_PENDING);
        }

        AssignmentExtension extension = extensionMapper.toEntity(request);

        extension.setAssignment(assignment);
        extension.setPreviousReturnAt(assignment.getExpectedReturnAt());
        extension.setStatus(ExtensionRequestStatus.PENDING);
        extension.setRequestedBy(currentUser.getEmail());
        extension.setRequestedAt(LocalDateTime.now());

        AssignmentExtension saved = extensionRepository.save(extension);

        notifyAdmins(saved);

        return extensionMapper.toResponse(saved);
    }

    @Override
    public List<ExtensionResponse> getMyRequests() {
        return extensionRepository
                .findByRequestedByOrderByRequestedAtDesc(getCurrentUser().getEmail())
                .stream()
                .map(extensionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ExtensionResponse approveRequest(UUID requestId, ExtensionReviewRequest request) {
        AssignmentExtension extension = getPendingRequest(requestId);
        DeviceAssignment assignment = extension.getAssignment();

        if (assignment.getStatus() == DeviceAssignmentStatus.RETURNED) {
            throw new AppException(ErrorCode.CANNOT_EXTEND_RETURNED_ASSIGNMENT);
        }

        LocalDateTime now = LocalDateTime.now();

        assignment.setExpectedReturnAt(extension.getRequestedReturnAt());
        assignment.setStatus(DeviceAssignmentStatus.ACTIVE);
        assignment.setReminderNotifiedAt(null);
        assignment.setOverdueNotifiedAt(null);

        extension.setStatus(ExtensionRequestStatus.APPROVED);
        extension.setReviewedBy(getCurrentUser().getEmail());
        extension.setReviewedAt(now);

        if (request != null) {
            extensionMapper.updateReview(request, extension);
        }

        sendResultEmail(extension, true);

        return extensionMapper.toResponse(extension);
    }

    @Override
    @Transactional
    public ExtensionResponse rejectRequest(UUID requestId, ExtensionReviewRequest request) {
        AssignmentExtension extension = getPendingRequest(requestId);

        extension.setStatus(ExtensionRequestStatus.REJECTED);
        extension.setReviewedBy(getCurrentUser().getEmail());
        extension.setReviewedAt(LocalDateTime.now());

        if (request != null) {
            extensionMapper.updateReview(request, extension);
        }

        sendResultEmail(extension, false);

        return extensionMapper.toResponse(extension);
    }

    @Override
    public List<ExtensionResponse> getPendingRequests() {
        return extensionRepository
                .findByStatusOrderByRequestedAtAsc(ExtensionRequestStatus.PENDING)
                .stream()
                .map(extensionMapper::toResponse)
                .toList();
    }

    private AssignmentExtension getPendingRequest(UUID requestId) {
        AssignmentExtension extension = extensionRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.EXTENSION_REQUEST_NOT_FOUND));

        if (extension.getStatus() != ExtensionRequestStatus.PENDING) {
            throw new AppException(ErrorCode.EXTENSION_REQUEST_ALREADY_REVIEWED);
        }

        return extension;
    }

    private void notifyAdmins(AssignmentExtension extension) {
        DeviceAssignment assignment = extension.getAssignment();

        String subject = "Yêu cầu gia hạn thiết bị mới";

        String content = "Người dùng " + assignment.getUser().getName()
                + " vừa gửi yêu cầu gia hạn thiết bị " + assignment.getDevice().getName() + ".\n\n"
                + "Hạn hiện tại: " + extension.getPreviousReturnAt()
                + "\nHạn đề xuất: " + extension.getRequestedReturnAt()
                + "\nLý do: " + extension.getReason();

        for (User admin : userRepository.findDistinctByRoles_Name("ADMIN")) {
            try {
                emailService.sendEmail(admin.getEmail(), subject, content);
            } catch (Exception e) {
                System.out.println("Không gửi được mail cho admin "
                        + admin.getEmail() + ": " + e.getMessage());
            }
        }
    }

    private void sendResultEmail(AssignmentExtension extension, boolean approved) {
        String subject = approved
                ? "Yêu cầu gia hạn đã được chấp nhận"
                : "Yêu cầu gia hạn đã bị từ chối";

        String content = approved
                ? "Yêu cầu gia hạn thiết bị " + extension.getAssignment().getDevice().getName()
                  + " đã được chấp nhận.\nHạn trả mới: " + extension.getRequestedReturnAt()
                : "Yêu cầu gia hạn thiết bị " + extension.getAssignment().getDevice().getName()
                  + " đã bị từ chối.\nHạn trả vẫn là: " + extension.getPreviousReturnAt();

        if (extension.getReviewNote() != null && !extension.getReviewNote().isBlank()) {
            content += "\nGhi chú: " + extension.getReviewNote();
        }

        try {
            emailService.sendEmail(extension.getRequestedBy(), subject, content);
        } catch (Exception e) {
            System.out.println("Không gửi được email kết quả: " + e.getMessage());
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}