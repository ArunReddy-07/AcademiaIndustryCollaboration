package com.academiaindustry.service.impl;

import com.academiaindustry.dto.SkillAssessmentRequest;
import com.academiaindustry.dto.SkillAssessmentResponse;
import com.academiaindustry.entity.ProficiencyLevel;
import com.academiaindustry.entity.Skill;
import com.academiaindustry.entity.SkillAssessment;
import com.academiaindustry.entity.Student;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.SkillAssessmentRepository;
import com.academiaindustry.repository.SkillRepository;
import com.academiaindustry.repository.StudentRepository;
import com.academiaindustry.service.SkillAssessmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SkillAssessmentServiceImpl implements SkillAssessmentService {

    private final SkillAssessmentRepository assessmentRepository;
    private final StudentRepository studentRepository;
    private final SkillRepository skillRepository;

    public SkillAssessmentServiceImpl(SkillAssessmentRepository assessmentRepository,
                                      StudentRepository studentRepository, SkillRepository skillRepository) {
        this.assessmentRepository = assessmentRepository;
        this.studentRepository = studentRepository;
        this.skillRepository = skillRepository;
    }

    @Override
    public SkillAssessmentResponse submit(SkillAssessmentRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));
        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + request.getSkillId()));
        ProficiencyLevel level = calculateLevel(request.getScore());
        assessmentRepository.saveOrUpdateScore(
                student.getId(), skill.getId(), request.getScore(), level.name());
        SkillAssessment assessment = assessmentRepository.findByStudentIdAndSkillId(
                        student.getId(), skill.getId())
                .orElseThrow(() -> new IllegalStateException("Saved assessment could not be retrieved."));
        return toResponse(assessment);
    }

    @Override
    public List<SkillAssessmentResponse> getMine(String email) {
        return assessmentRepository.findByStudentUserEmailIgnoreCase(email)
                .stream().map(this::toResponse).toList();
    }

    private ProficiencyLevel calculateLevel(int score) {
        if (score >= 75) return ProficiencyLevel.EXPERT;
        if (score >= 50) return ProficiencyLevel.ADVANCED;
        if (score >= 25) return ProficiencyLevel.INTERMEDIATE;
        return ProficiencyLevel.BEGINNER;
    }

    private SkillAssessmentResponse toResponse(SkillAssessment assessment) {
        SkillAssessmentResponse response = new SkillAssessmentResponse();
        response.setId(assessment.getId());
        response.setStudentId(assessment.getStudent().getId());
        response.setSkillId(assessment.getSkill().getId());
        response.setScore(assessment.getScore());
        response.setLevel(assessment.getLevel());
        response.setAssessedAt(assessment.getAssessedAt());
        return response;
    }
}