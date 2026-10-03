package com.academiaindustry.controller;

import com.academiaindustry.dto.SkillMatchResponse;
import com.academiaindustry.entity.OpportunityType;
import com.academiaindustry.service.SkillMatchingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skill-gaps")
@PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
public class SkillGapController {

    private final SkillMatchingService matchingService;

    public SkillGapController(SkillMatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping("/{opportunityId}")
    public SkillMatchResponse getMatch(Authentication authentication, @PathVariable Long opportunityId) {
        return matchingService.match(authentication.getName(), opportunityId);
    }

    @GetMapping("/{type}/{listingId}")
    public SkillMatchResponse getCareerListingMatch(Authentication authentication,
                                                    @PathVariable OpportunityType type,
                                                    @PathVariable Long listingId) {
        return matchingService.match(authentication.getName(), type, listingId);
    }
}