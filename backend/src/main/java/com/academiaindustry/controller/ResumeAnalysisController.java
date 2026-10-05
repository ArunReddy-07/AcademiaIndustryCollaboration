package com.academiaindustry.controller;

import com.academiaindustry.dto.ResumeAnalysisResponse;
import com.academiaindustry.service.ResumeAnalysisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume-analysis")
@PreAuthorize("hasRole('STUDENT')")
public class ResumeAnalysisController {

    private final ResumeAnalysisService resumeAnalysisService;

    public ResumeAnalysisController(ResumeAnalysisService resumeAnalysisService) {
        this.resumeAnalysisService = resumeAnalysisService;
    }

    @GetMapping("/me")
    public ResumeAnalysisResponse getMine(Authentication authentication) {
        return resumeAnalysisService.getForStudent(authentication.getName());
    }

    @PostMapping(value = "/me/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResumeAnalysisResponse analyze(Authentication authentication,
                                        @RequestParam("file") MultipartFile file) {
        return resumeAnalysisService.analyze(authentication.getName(), file);
    }
}
