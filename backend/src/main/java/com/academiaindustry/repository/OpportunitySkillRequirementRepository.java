package com.academiaindustry.repository;

import com.academiaindustry.entity.OpportunitySkillRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpportunitySkillRequirementRepository extends JpaRepository<OpportunitySkillRequirement, Long> {

    List<OpportunitySkillRequirement> findByOpportunityId(Long opportunityId);

    boolean existsByOpportunityIdAndSkillId(Long opportunityId, Long skillId);
}