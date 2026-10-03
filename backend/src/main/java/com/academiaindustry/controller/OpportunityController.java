package com.academiaindustry.controller;

import com.academiaindustry.dto.OpportunityRequest;
import com.academiaindustry.dto.OpportunityResponse;
import com.academiaindustry.entity.OpportunityType;
import com.academiaindustry.service.OpportunityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {

    private final OpportunityService opportunityService;

    public OpportunityController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @GetMapping
    public List<OpportunityResponse> getAll() {
        return opportunityService.getAll();
    }

    @GetMapping("/{id}")
    public OpportunityResponse getById(@PathVariable Long id) {
        return opportunityService.getById(id);
    }

    @GetMapping("/projects")
    public List<OpportunityResponse> getProjects() {
        return opportunityService.getByType(OpportunityType.PROJECT);
    }

    @GetMapping("/apprenticeships")
    public List<OpportunityResponse> getApprenticeships() {
        return opportunityService.getByType(OpportunityType.APPRENTICESHIP);
    }

    @GetMapping("/programs")
    public List<OpportunityResponse> getPrograms() {
        return opportunityService.getByType(OpportunityType.PROGRAM);
    }

    @PostMapping
        @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('INDUSTRY') and @ownershipSecurity.isUser(authentication.name, #request.industryId))")
    public ResponseEntity<OpportunityResponse> create(@Valid @RequestBody OpportunityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(opportunityService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('INDUSTRY') and @ownershipSecurity.isOpportunityOwner(#id, authentication.name))")
    public OpportunityResponse update(@PathVariable Long id, @Valid @RequestBody OpportunityRequest request) {
        return opportunityService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('INDUSTRY') and @ownershipSecurity.isOpportunityOwner(#id, authentication.name))")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        opportunityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}