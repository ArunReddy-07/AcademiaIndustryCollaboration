package com.academiaindustry.controller;

import com.academiaindustry.dto.PlacementRequest;
import com.academiaindustry.dto.PlacementResponse;
import com.academiaindustry.security.OwnershipSecurity;
import com.academiaindustry.service.PlacementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/placements")
public class PlacementController {

    private final PlacementService placementService;
    private final OwnershipSecurity ownershipSecurity;

    public PlacementController(PlacementService placementService, OwnershipSecurity ownershipSecurity) {
        this.placementService = placementService;
        this.ownershipSecurity = ownershipSecurity;
    }

    @PostMapping
        @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('INDUSTRY') and @ownershipSecurity.isApplicationOpportunityOwner(#request.applicationId, authentication.name))")
        public ResponseEntity<PlacementResponse> create(@Valid @RequestBody PlacementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(placementService.create(request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public List<PlacementResponse> getMine(Authentication authentication) {
        return placementService.getMine(authentication.getName());
    }

    @GetMapping("/industry")
    @PreAuthorize("hasRole('INDUSTRY')")
    public List<PlacementResponse> getForIndustry(Authentication authentication) {
        return placementService.getAll().stream()
                .filter(placement -> ownershipSecurity.isApplicationOpportunityOwner(
                        placement.getApplicationId(), authentication.getName()))
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INDUSTRY', 'STUDENT')")
    public PlacementResponse getById(Authentication authentication, @PathVariable Long id) {
        boolean administrator = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!administrator && !ownershipSecurity.isPlacementAccessible(id, authentication.getName())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to view this placement.");
        }
        return placementService.getById(id);
    }

    @PutMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('INDUSTRY') and @ownershipSecurity.isPlacementAccessible(#id, authentication.name))")
        public PlacementResponse update(@PathVariable Long id, @Valid @RequestBody PlacementRequest request) {
        return placementService.update(id, request);
    }
}