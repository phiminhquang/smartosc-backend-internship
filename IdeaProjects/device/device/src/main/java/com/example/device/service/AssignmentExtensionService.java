package com.example.device.service;

import com.example.device.dto.request.ExtensionRequestCreationRequest;
import com.example.device.dto.request.ExtensionReviewRequest;
import com.example.device.dto.response.ExtensionResponse;
import com.example.device.dto.response.PageResult;
import com.example.device.enums.ExtensionRequestStatus;

import java.util.UUID;

public interface AssignmentExtensionService {

    ExtensionResponse createRequest(UUID assignmentId, ExtensionRequestCreationRequest request);

    ExtensionResponse approveRequest(UUID requestId, ExtensionReviewRequest request);

    ExtensionResponse rejectRequest(UUID requestId, ExtensionReviewRequest request);

    PageResult<ExtensionResponse> getPendingRequests(
            int page,
            int size,
            String sort
    );

    PageResult<ExtensionResponse> getMyRequests(
            ExtensionRequestStatus status,
            int page,
            int size,
            String sort
    );
}
