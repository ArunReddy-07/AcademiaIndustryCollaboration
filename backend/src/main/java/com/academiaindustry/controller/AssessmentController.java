package com.academiaindustry.controller;

import com.academiaindustry.dto.SkillAssessmentRequest;
import com.academiaindustry.dto.SkillAssessmentResponse;
import com.academiaindustry.service.SkillAssessmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assessments")
@PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
public class AssessmentController {

    private final SkillAssessmentService assessmentService;

    public AssessmentController(SkillAssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping
        @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('STUDENT') and @ownershipSecurity.isStudentOwner(#request.studentId, authentication.name))")
    public SkillAssessmentResponse submit(@Valid @RequestBody SkillAssessmentRequest request) {
        return assessmentService.submit(request);
    }

    @GetMapping("/me")
    public List<SkillAssessmentResponse> getMine(Authentication authentication) {
        return assessmentService.getMine(authentication.getName());
    }
}