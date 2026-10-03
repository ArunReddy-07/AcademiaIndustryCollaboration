package com.academiaindustry.controller;

import com.academiaindustry.dto.SkillMatchResponse;
import com.academiaindustry.service.SkillMatchingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@PreAuthorize("hasRole('STUDENT')")
public class RecommendationController {

    private final SkillMatchingService skillMatchingService;

    public RecommendationController(SkillMatchingService skillMatchingService) {
        this.skillMatchingService = skillMatchingService;
    }

    @GetMapping
    public List<SkillMatchResponse> getRecommendations(Authentication authentication) {
        return skillMatchingService.recommendations(authentication.getName());
    }
}