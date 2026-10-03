package com.academiaindustry.service;

import com.academiaindustry.dto.CollaborationRequest;
import com.academiaindustry.dto.CollaborationResponse;
import com.academiaindustry.entity.CollaborationStatus;

import java.util.List;

public interface CollaborationService {
    CollaborationResponse create(CollaborationRequest request, String email, boolean administrator);
    CollaborationResponse getById(Long id, String email, boolean administrator);
    List<CollaborationResponse> getAll(String email, boolean administrator);
    CollaborationResponse updateStatus(Long id, CollaborationStatus status, String email, boolean administrator);
}