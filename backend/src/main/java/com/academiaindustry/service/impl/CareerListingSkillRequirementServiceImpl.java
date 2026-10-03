package com.academiaindustry.service.impl;

import com.academiaindustry.dto.CareerSkillRequirementResponse;
import com.academiaindustry.dto.CareerSkillRequirementUpdateRequest;
import com.academiaindustry.dto.SkillRequirementRequest;
import com.academiaindustry.entity.CareerListingSkillRequirement;
import com.academiaindustry.entity.Internship;
import com.academiaindustry.entity.Job;
import com.academiaindustry.entity.OpportunityType;
import com.academiaindustry.entity.Skill;
import com.academiaindustry.exception.DuplicateResourceException;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.CareerListingSkillRequirementRepository;
import com.academiaindustry.repository.InternshipRepository;
import com.academiaindustry.repository.JobRepository;
import com.academiaindustry.repository.SkillRepository;
import com.academiaindustry.service.CareerListingSkillRequirementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CareerListingSkillRequirementServiceImpl implements CareerListingSkillRequirementService {

    private final CareerListingSkillRequirementRepository requirementRepository;
    private final InternshipRepository internshipRepository;
    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;

    public CareerListingSkillRequirementServiceImpl(CareerListingSkillRequirementRepository requirementRepository,
                                                    InternshipRepository internshipRepository,
                                                    JobRepository jobRepository,
                                                    SkillRepository skillRepository) {
        this.requirementRepository = requirementRepository;
        this.internshipRepository = internshipRepository;
        this.jobRepository = jobRepository;
        this.skillRepository = skillRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CareerSkillRequirementResponse> getForListing(OpportunityType type, Long listingId) {
        return findAll(type, listingId).stream().map(this::toResponse).toList();
    }

    @Override
    public CareerSkillRequirementResponse add(OpportunityType type, Long listingId,
                                               SkillRequirementRequest request) {
        Internship internship = type == OpportunityType.INTERNSHIP ? findInternship(listingId) : null;
        Job job = type == OpportunityType.JOB ? findJob(listingId) : null;
        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + request.getSkillId()));
        boolean duplicate = type == OpportunityType.INTERNSHIP
                ? requirementRepository.existsByInternshipIdAndSkillId(listingId, request.getSkillId())
                : requirementRepository.existsByJobIdAndSkillId(listingId, request.getSkillId());
        if (duplicate) throw new DuplicateResourceException("This skill is already required by the listing.");
        return toResponse(requirementRepository.save(new CareerListingSkillRequirement(
                internship, job, skill, request.getRequiredLevel())));
    }

    @Override
    public CareerSkillRequirementResponse update(OpportunityType type, Long listingId, Long requirementId,
                                                  CareerSkillRequirementUpdateRequest request) {
        CareerListingSkillRequirement requirement = findRequirement(type, listingId, requirementId);
        requirement.setRequiredLevel(request.getRequiredLevel());
        return toResponse(requirementRepository.save(requirement));
    }

    @Override
    public void delete(OpportunityType type, Long listingId, Long requirementId) {
        requirementRepository.delete(findRequirement(type, listingId, requirementId));
    }

    private List<CareerListingSkillRequirement> findAll(OpportunityType type, Long listingId) {
        return switch (type) {
            case INTERNSHIP -> {
                findInternship(listingId);
                yield requirementRepository.findByInternshipIdOrderById(listingId);
            }
            case JOB -> {
                findJob(listingId);
                yield requirementRepository.findByJobIdOrderById(listingId);
            }
            default -> throw new IllegalArgumentException("Only internship and job listings accept career skill requirements.");
        };
    }

    private CareerListingSkillRequirement findRequirement(OpportunityType type, Long listingId, Long requirementId) {
        return switch (type) {
            case INTERNSHIP -> requirementRepository.findByIdAndInternshipId(requirementId, listingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Skill requirement not found."));
            case JOB -> requirementRepository.findByIdAndJobId(requirementId, listingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Skill requirement not found."));
            default -> throw new IllegalArgumentException("Only internship and job listings accept career skill requirements.");
        };
    }

    private Internship findInternship(Long id) {
        return internshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found with id: " + id));
    }

    private Job findJob(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }

    private CareerSkillRequirementResponse toResponse(CareerListingSkillRequirement requirement) {
        CareerSkillRequirementResponse response = new CareerSkillRequirementResponse();
        response.setId(requirement.getId());
        response.setListingType(requirement.getInternship() == null ? OpportunityType.JOB : OpportunityType.INTERNSHIP);
        response.setListingId(requirement.getInternship() == null
                ? requirement.getJob().getId() : requirement.getInternship().getId());
        response.setSkillId(requirement.getSkill().getId());
        response.setSkillName(requirement.getSkill().getName());
        response.setRequiredLevel(requirement.getRequiredLevel());
        return response;
    }
}