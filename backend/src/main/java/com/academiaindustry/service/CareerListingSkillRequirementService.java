package com.academiaindustry.service;

import com.academiaindustry.dto.CareerSkillRequirementResponse;
import com.academiaindustry.dto.CareerSkillRequirementUpdateRequest;
import com.academiaindustry.dto.SkillRequirementRequest;
import com.academiaindustry.entity.OpportunityType;

import java.util.List;

public interface CareerListingSkillRequirementService {

    List<CareerSkillRequirementResponse> getForListing(OpportunityType type, Long listingId);

    CareerSkillRequirementResponse add(OpportunityType type, Long listingId, SkillRequirementRequest request);

    CareerSkillRequirementResponse update(OpportunityType type, Long listingId, Long requirementId,
                                          CareerSkillRequirementUpdateRequest request);

    void delete(OpportunityType type, Long listingId, Long requirementId);
}