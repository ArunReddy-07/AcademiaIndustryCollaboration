package com.academiaindustry.service;

import com.academiaindustry.dto.ApplicationRequest;
import com.academiaindustry.dto.ApplicationResponse;
import com.academiaindustry.entity.ApplicationStatus;
import com.academiaindustry.entity.OpportunityType;

import java.util.List;

public interface ApplicationService {

    ApplicationResponse create(ApplicationRequest request);

    ApplicationResponse getById(Long id);

    List<ApplicationResponse> getAll();

    List<ApplicationResponse> getMine(String email);

    List<ApplicationResponse> getForOpportunity(String email, OpportunityType type, Long opportunityId,
                                                 boolean administrator);

    ApplicationResponse updateStatus(Long id, ApplicationStatus status, String email, boolean administrator);

    ApplicationResponse updateSharedPortfolioItems(Long id, List<Long> itemIds, String email);
}