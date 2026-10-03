package com.academiaindustry.service.impl;

import com.academiaindustry.dto.SkillRequirementRequest;
import com.academiaindustry.dto.SkillRequirementResponse;
import com.academiaindustry.entity.Opportunity;
import com.academiaindustry.entity.OpportunitySkillRequirement;
import com.academiaindustry.entity.Skill;
import com.academiaindustry.exception.DuplicateResourceException;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.OpportunityRepository;
import com.academiaindustry.repository.OpportunitySkillRequirementRepository;
import com.academiaindustry.repository.SkillRepository;
import com.academiaindustry.service.SkillRequirementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SkillRequirementServiceImpl implements SkillRequirementService {

    private final OpportunitySkillRequirementRepository requirementRepository;
    private final OpportunityRepository opportunityRepository;
    private final SkillRepository skillRepository;

    public SkillRequirementServiceImpl(OpportunitySkillRequirementRepository requirementRepository,
                                       OpportunityRepository opportunityRepository,
                                       SkillRepository skillRepository) {
        this.requirementRepository = requirementRepository;
        this.opportunityRepository = opportunityRepository;
        this.skillRepository = skillRepository;
    }

    @Override
    public SkillRequirementResponse add(Long opportunityId, SkillRequirementRequest request) {
        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + opportunityId));
        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + request.getSkillId()));
        if (requirementRepository.existsByOpportunityIdAndSkillId(opportunityId, request.getSkillId())) {
            throw new DuplicateResourceException("This skill is already required by the opportunity.");
        }
        return toResponse(requirementRepository.save(
                new OpportunitySkillRequirement(opportunity, skill, request.getRequiredLevel())));
    }

    @Override
    public List<SkillRequirementResponse> getForOpportunity(Long opportunityId) {
        if (!opportunityRepository.existsById(opportunityId)) {
            throw new ResourceNotFoundException("Opportunity not found with id: " + opportunityId);
        }
        return requirementRepository.findByOpportunityId(opportunityId).stream()
                .map(this::toResponse).toList();
    }

    private SkillRequirementResponse toResponse(OpportunitySkillRequirement requirement) {
        SkillRequirementResponse response = new SkillRequirementResponse();
        response.setId(requirement.getId());
        response.setOpportunityId(requirement.getOpportunity().getId());
        response.setSkillId(requirement.getSkill().getId());
        response.setSkillName(requirement.getSkill().getName());
        response.setRequiredLevel(requirement.getRequiredLevel());
        return response;
    }
}