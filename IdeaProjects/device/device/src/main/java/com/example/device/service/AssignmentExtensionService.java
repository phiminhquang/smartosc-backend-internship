package com.example.device.service;

import com.example.device.dto.request.ExtensionRequestCreationRequest;
import com.example.device.dto.request.ExtensionReviewRequest;
import com.example.device.dto.response.ExtensionResponse;

import java.util.List;
import java.util.UUID;

public interface AssignmentExtensionService {

    ExtensionResponse createRequest(UUID assignmentId, ExtensionRequestCreationRequest request);

    ExtensionResponse approveRequest(UUID requestId, ExtensionReviewRequest request);

    ExtensionResponse rejectRequest(UUID requestId, ExtensionReviewRequest request);

    List<ExtensionResponse> getPendingRequests();

    List<ExtensionResponse> getMyRequests();
}