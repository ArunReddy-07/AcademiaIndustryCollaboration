package com.academiaindustry.controller;

import com.academiaindustry.dto.SkillRequirementRequest;
import com.academiaindustry.dto.SkillRequirementResponse;
import com.academiaindustry.service.SkillRequirementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/opportunities/{opportunityId}/skills")
public class SkillRequirementController {

    private final SkillRequirementService requirementService;

    public SkillRequirementController(SkillRequirementService requirementService) {
        this.requirementService = requirementService;
    }

    @GetMapping
    public List<SkillRequirementResponse> getForOpportunity(@PathVariable Long opportunityId) {
        return requirementService.getForOpportunity(opportunityId);
    }

    @PostMapping
        @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('INDUSTRY') and @ownershipSecurity.isOpportunityOwner(#opportunityId, authentication.name))")
    public ResponseEntity<SkillRequirementResponse> add(
            Authentication authentication, @PathVariable Long opportunityId,
            @Valid @RequestBody SkillRequirementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(requirementService.add(opportunityId, request));
    }
}