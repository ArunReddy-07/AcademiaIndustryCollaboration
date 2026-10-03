package com.academiaindustry.service;

import com.academiaindustry.dto.SkillMatchResponse;
import com.academiaindustry.entity.OpportunityType;

import java.util.List;

public interface SkillMatchingService {

    SkillMatchResponse match(String studentEmail, Long opportunityId);

    SkillMatchResponse match(String studentEmail, OpportunityType type, Long listingId);

    List<SkillMatchResponse> recommendations(String studentEmail);
}