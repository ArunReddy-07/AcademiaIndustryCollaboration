package com.academiaindustry.service.impl;

import com.academiaindustry.dto.OpportunityRequest;
import com.academiaindustry.dto.OpportunityResponse;
import com.academiaindustry.entity.Opportunity;
import com.academiaindustry.entity.OpportunityType;
import com.academiaindustry.entity.User;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.OpportunityRepository;
import com.academiaindustry.repository.UserRepository;
import com.academiaindustry.service.OpportunityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OpportunityServiceImpl implements OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final UserRepository userRepository;

    public OpportunityServiceImpl(OpportunityRepository opportunityRepository, UserRepository userRepository) {
        this.opportunityRepository = opportunityRepository;
        this.userRepository = userRepository;
    }

    @Override
    public OpportunityResponse create(OpportunityRequest request) {
        Opportunity opportunity = new Opportunity(request.getType(), findUser(request.getIndustryId()),
                request.getTitle(), request.getDescription(), request.getLocation(), request.getDuration(),
                request.getStipend(), request.getApplicationDeadline(), request.getStatus());
        return toResponse(opportunityRepository.save(opportunity));
    }

    @Override
    public OpportunityResponse getById(Long id) {
        return toResponse(findOpportunity(id));
    }

    @Override
    public List<OpportunityResponse> getAll() {
        return opportunityRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<OpportunityResponse> getByType(OpportunityType type) {
        return opportunityRepository.findByType(type).stream().map(this::toResponse).toList();
    }

    @Override
    public OpportunityResponse update(Long id, OpportunityRequest request) {
        Opportunity opportunity = findOpportunity(id);
        opportunity.setType(request.getType());
        opportunity.setIndustry(findUser(request.getIndustryId()));
        opportunity.setTitle(request.getTitle());
        opportunity.setDescription(request.getDescription());
        opportunity.setLocation(request.getLocation());
        opportunity.setDuration(request.getDuration());
        opportunity.setStipend(request.getStipend());
        opportunity.setApplicationDeadline(request.getApplicationDeadline());
        opportunity.setStatus(request.getStatus());
        return toResponse(opportunityRepository.save(opportunity));
    }

    @Override
    public void delete(Long id) {
        opportunityRepository.delete(findOpportunity(id));
    }

    private Opportunity findOpportunity(Long id) {
        return opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private OpportunityResponse toResponse(Opportunity opportunity) {
        OpportunityResponse response = new OpportunityResponse();
        response.setId(opportunity.getId());
        response.setType(opportunity.getType());
        response.setIndustryId(opportunity.getIndustry().getId());
        response.setCompanyName(companyName(opportunity.getIndustry()));
        response.setCompanyWebsite(opportunity.getIndustry().getWebsite());
        response.setIndustrySector(opportunity.getIndustry().getIndustrySector());
        response.setTitle(opportunity.getTitle());
        response.setDescription(opportunity.getDescription());
        response.setLocation(opportunity.getLocation());
        response.setDuration(opportunity.getDuration());
        response.setStipend(opportunity.getStipend());
        response.setApplicationDeadline(opportunity.getApplicationDeadline());
        response.setStatus(opportunity.getStatus());
        response.setCreatedAt(opportunity.getCreatedAt());
        response.setUpdatedAt(opportunity.getUpdatedAt());
        return response;
    }

    private String companyName(User industry) {
        return industry.getCompanyName() == null || industry.getCompanyName().isBlank()
                ? industry.getName() : industry.getCompanyName();
    }
}