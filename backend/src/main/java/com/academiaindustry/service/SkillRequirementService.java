package com.academiaindustry.service;

import com.academiaindustry.dto.SkillRequirementRequest;
import com.academiaindustry.dto.SkillRequirementResponse;

import java.util.List;

public interface SkillRequirementService {

    SkillRequirementResponse add(Long opportunityId, SkillRequirementRequest request);

    List<SkillRequirementResponse> getForOpportunity(Long opportunityId);
}