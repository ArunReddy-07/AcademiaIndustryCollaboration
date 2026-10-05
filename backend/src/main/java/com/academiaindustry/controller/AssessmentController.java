package com.academiaindustry.controller;

import com.academiaindustry.dto.SkillAssessmentRequest;
import com.academiaindustry.dto.SkillAssessmentResponse;
import com.academiaindustry.dto.AssessmentAttemptResponse;
import com.academiaindustry.dto.GenerateAssessmentRequest;
import com.academiaindustry.dto.SubmitAssessmentRequest;
import com.academiaindustry.service.AssessmentAttemptService;
import com.academiaindustry.service.SkillAssessmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final AssessmentAttemptService assessmentAttemptService;

    public AssessmentController(SkillAssessmentService assessmentService,
                                AssessmentAttemptService assessmentAttemptService) {
        this.assessmentService = assessmentService;
        this.assessmentAttemptService = assessmentAttemptService;
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('STUDENT')")
    public AssessmentAttemptResponse generate(Authentication authentication,
                                               @Valid @RequestBody GenerateAssessmentRequest request) {
        return assessmentAttemptService.generate(authentication.getName(), request);
    }

    @GetMapping("/attempts/{attemptId}")
    @PreAuthorize("hasRole('STUDENT')")
    public AssessmentAttemptResponse getAttempt(Authentication authentication, @PathVariable Long attemptId) {
        return assessmentAttemptService.getAttempt(authentication.getName(), attemptId);
    }

    @PostMapping("/attempts/{attemptId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public AssessmentAttemptResponse submitAttempt(Authentication authentication,
                                                    @PathVariable Long attemptId,
                                                    @Valid @RequestBody SubmitAssessmentRequest request) {
        return assessmentAttemptService.submit(authentication.getName(), attemptId, request);
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