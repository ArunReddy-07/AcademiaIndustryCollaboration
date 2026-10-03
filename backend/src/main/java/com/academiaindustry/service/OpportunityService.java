package com.academiaindustry.service;

import com.academiaindustry.dto.OpportunityRequest;
import com.academiaindustry.dto.OpportunityResponse;
import com.academiaindustry.entity.OpportunityType;

import java.util.List;

public interface OpportunityService {

    OpportunityResponse create(OpportunityRequest request);

    OpportunityResponse getById(Long id);

    List<OpportunityResponse> getAll();

    List<OpportunityResponse> getByType(OpportunityType type);

    OpportunityResponse update(Long id, OpportunityRequest request);

    void delete(Long id);
}