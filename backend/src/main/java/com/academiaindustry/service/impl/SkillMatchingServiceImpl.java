package com.academiaindustry.service.impl;

import com.academiaindustry.dto.SkillMatchResponse;
import com.academiaindustry.entity.OpportunitySkillRequirement;
import com.academiaindustry.entity.OpportunityStatus;
import com.academiaindustry.entity.OpportunityType;
import com.academiaindustry.entity.CareerListingSkillRequirement;
import com.academiaindustry.entity.Internship;
import com.academiaindustry.entity.Job;
import com.academiaindustry.entity.ProficiencyLevel;
import com.academiaindustry.entity.Skill;
import com.academiaindustry.entity.Student;
import com.academiaindustry.entity.StudentSkill;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.OpportunityRepository;
import com.academiaindustry.repository.OpportunitySkillRequirementRepository;
import com.academiaindustry.repository.CareerListingSkillRequirementRepository;
import com.academiaindustry.repository.InternshipRepository;
import com.academiaindustry.repository.JobRepository;
import com.academiaindustry.repository.StudentRepository;
import com.academiaindustry.repository.StudentSkillRepository;
import com.academiaindustry.service.SkillMatchingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class SkillMatchingServiceImpl implements SkillMatchingService {

    private final StudentRepository studentRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final OpportunityRepository opportunityRepository;
    private final OpportunitySkillRequirementRepository requirementRepository;
    private final CareerListingSkillRequirementRepository careerRequirementRepository;
    private final InternshipRepository internshipRepository;
    private final JobRepository jobRepository;

    public SkillMatchingServiceImpl(StudentRepository studentRepository, StudentSkillRepository studentSkillRepository,
                                    OpportunityRepository opportunityRepository,
                                    OpportunitySkillRequirementRepository requirementRepository,
                                    CareerListingSkillRequirementRepository careerRequirementRepository,
                                    InternshipRepository internshipRepository, JobRepository jobRepository) {
        this.studentRepository = studentRepository;
        this.studentSkillRepository = studentSkillRepository;
        this.opportunityRepository = opportunityRepository;
        this.requirementRepository = requirementRepository;
        this.careerRequirementRepository = careerRequirementRepository;
        this.internshipRepository = internshipRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    public SkillMatchResponse match(String studentEmail, Long opportunityId) {
        Student student = studentRepository.findByUserEmailIgnoreCase(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));
        var opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + opportunityId));
        return match(studentEmail, student, opportunity.getType(), opportunityId);
    }

    @Override
    public SkillMatchResponse match(String studentEmail, OpportunityType type, Long listingId) {
        Student student = studentRepository.findByUserEmailIgnoreCase(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));
        if (type != OpportunityType.INTERNSHIP && type != OpportunityType.JOB
                && type != OpportunityType.PROJECT && type != OpportunityType.APPRENTICESHIP
                && type != OpportunityType.PROGRAM) {
            throw new IllegalArgumentException("Unsupported listing type.");
        }
        if (type == OpportunityType.INTERNSHIP && internshipRepository.findById(listingId).isEmpty()) {
            throw new ResourceNotFoundException("Internship not found with id: " + listingId);
        }
        if (type == OpportunityType.JOB && jobRepository.findById(listingId).isEmpty()) {
            throw new ResourceNotFoundException("Job not found with id: " + listingId);
        }
        if (type != OpportunityType.INTERNSHIP && type != OpportunityType.JOB
                && opportunityRepository.findById(listingId).isEmpty()) {
            throw new ResourceNotFoundException("Opportunity not found with id: " + listingId);
        }
        return match(studentEmail, student, type, listingId);
    }

    private SkillMatchResponse match(String studentEmail, Student student, OpportunityType type, Long listingId) {
        Map<String, ProficiencyLevel> studentSkills = new HashMap<>();
        for (StudentSkill studentSkill : studentSkillRepository.findByStudentUserEmailIgnoreCase(studentEmail)) {
            studentSkills.put(normalize(studentSkill.getSkill()), studentSkill.getProficiencyLevel());
        }

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        List<String> insufficient = new ArrayList<>();
        List<Skill> requiredSkills = new ArrayList<>();
        List<ProficiencyLevel> requiredLevels = new ArrayList<>();
        switch (type) {
            case INTERNSHIP -> careerRequirementRepository.findByInternshipIdOrderById(listingId)
                    .forEach(requirement -> {
                        requiredSkills.add(requirement.getSkill());
                        requiredLevels.add(requirement.getRequiredLevel());
                    });
            case JOB -> careerRequirementRepository.findByJobIdOrderById(listingId)
                    .forEach(requirement -> {
                        requiredSkills.add(requirement.getSkill());
                        requiredLevels.add(requirement.getRequiredLevel());
                    });
            case PROJECT, APPRENTICESHIP, PROGRAM -> requirementRepository.findByOpportunityId(listingId)
                    .forEach(requirement -> {
                        requiredSkills.add(requirement.getSkill());
                        requiredLevels.add(requirement.getRequiredLevel());
                    });
        }
        for (int index = 0; index < requiredSkills.size(); index++) {
            Skill skill = requiredSkills.get(index);
            String skillName = skill.getName();
            ProficiencyLevel studentLevel = studentSkills.get(normalize(skill));
            if (studentLevel == null) {
                missing.add(skillName);
            } else if (studentLevel.ordinal() >= requiredLevels.get(index).ordinal()) {
                matched.add(skillName);
            } else {
                insufficient.add(skillName);
            }
        }

        int requiredCount = matched.size() + missing.size() + insufficient.size();
        int percentage = requiredCount == 0 ? 0 : (matched.size() * 100) / requiredCount;
        SkillMatchResponse response = new SkillMatchResponse();
        response.setOpportunityId(listingId);
        response.setOpportunityType(type);
        response.setStudentId(student.getId());
        response.setMatchedCount(matched.size());
        response.setRequiredCount(requiredCount);
        response.setMatchPercentage(percentage);
        response.setMatchedSkills(matched);
        response.setMissingSkills(missing);
        response.setInsufficientProficiencySkills(insufficient);
        return response;
    }

    @Override
    public List<SkillMatchResponse> recommendations(String studentEmail) {
        studentRepository.findByUserEmailIgnoreCase(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));
        List<SkillMatchResponse> matches = new ArrayList<>();
        opportunityRepository.findAll().stream()
            .filter(opportunity -> isOpen(opportunity.getStatus(), opportunity.getApplicationDeadline()))
            .map(opportunity -> match(studentEmail, opportunity.getType(), opportunity.getId()))
            .forEach(matches::add);
        internshipRepository.findAll().stream()
            .filter(internship -> isOpen(internship.getStatus(), internship.getApplicationDeadline()))
            .map(internship -> match(studentEmail, OpportunityType.INTERNSHIP, internship.getId()))
            .forEach(matches::add);
        jobRepository.findAll().stream()
            .filter(job -> isOpen(job.getStatus(), job.getApplicationDeadline()))
            .map(job -> match(studentEmail, OpportunityType.JOB, job.getId()))
            .forEach(matches::add);
        return matches.stream()
                .sorted(Comparator.comparingInt((SkillMatchResponse response) -> response.getMatchPercentage()).reversed())
                .toList();
    }

    private boolean isOpen(OpportunityStatus status, LocalDate deadline) {
        return status == OpportunityStatus.OPEN && deadline != null && !deadline.isBefore(LocalDate.now());
    }

    private String normalize(Skill skill) {
        return skill.getName().trim().toLowerCase(Locale.ROOT);
    }
}