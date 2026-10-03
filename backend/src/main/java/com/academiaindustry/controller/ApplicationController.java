package com.academiaindustry.controller;

import com.academiaindustry.dto.ApplicationRequest;
import com.academiaindustry.dto.ApplicationResponse;
import com.academiaindustry.dto.ApplicationStatusRequest;
import com.academiaindustry.dto.SharedPortfolioItemsRequest;
import com.academiaindustry.entity.OpportunityType;
import com.academiaindustry.security.OwnershipSecurity;
import com.academiaindustry.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final OwnershipSecurity ownershipSecurity;

    public ApplicationController(ApplicationService applicationService, OwnershipSecurity ownershipSecurity) {
        this.applicationService = applicationService;
        this.ownershipSecurity = ownershipSecurity;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('STUDENT') and @ownershipSecurity.isStudentOwner(#request.studentId, authentication.name))")
    public ResponseEntity<ApplicationResponse> create(@Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.create(request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public List<ApplicationResponse> getMine(Authentication authentication) {
        return applicationService.getMine(authentication.getName());
    }

    @GetMapping("/opportunity/{type}/{opportunityId}")
    @PreAuthorize("hasAnyRole('INDUSTRY', 'ADMIN')")
    public List<ApplicationResponse> getForOpportunity(
            Authentication authentication, @PathVariable OpportunityType type, @PathVariable Long opportunityId) {
        return applicationService.getForOpportunity(authentication.getName(), type, opportunityId,
                isAdministrator(authentication));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('INDUSTRY', 'ADMIN')")
    public ApplicationResponse updateStatus(Authentication authentication, @PathVariable Long id,
                                            @Valid @RequestBody ApplicationStatusRequest request) {
        return applicationService.updateStatus(id, request.getStatus(), authentication.getName(),
                isAdministrator(authentication));
    }

    @PutMapping("/{id}/shared-portfolio-items")
    @PreAuthorize("hasRole('STUDENT')")
    public ApplicationResponse updateSharedPortfolioItems(Authentication authentication, @PathVariable Long id,
                                                           @Valid @RequestBody SharedPortfolioItemsRequest request) {
        return applicationService.updateSharedPortfolioItems(
                id, request.getSharedPortfolioItemIds(), authentication.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'INDUSTRY', 'ADMIN')")
    public ApplicationResponse getById(Authentication authentication, @PathVariable Long id) {
        if (!isAdministrator(authentication)
                && !ownershipSecurity.isApplicationAccessible(id, authentication.getName())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to view this application.");
        }
        return applicationService.getById(id);
    }

    private boolean isAdministrator(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}