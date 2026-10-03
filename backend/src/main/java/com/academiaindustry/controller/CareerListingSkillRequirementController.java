package com.academiaindustry.controller;

import com.academiaindustry.dto.CareerSkillRequirementResponse;
import com.academiaindustry.dto.CareerSkillRequirementUpdateRequest;
import com.academiaindustry.dto.SkillRequirementRequest;
import com.academiaindustry.entity.OpportunityType;
import com.academiaindustry.security.OwnershipSecurity;
import com.academiaindustry.service.CareerListingSkillRequirementService;
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
@RequestMapping("/api/career-listings/{type}/{listingId}/skills")
public class CareerListingSkillRequirementController {

    private final CareerListingSkillRequirementService requirementService;
    private final OwnershipSecurity ownershipSecurity;

    public CareerListingSkillRequirementController(CareerListingSkillRequirementService requirementService,
                                                   OwnershipSecurity ownershipSecurity) {
        this.requirementService = requirementService;
        this.ownershipSecurity = ownershipSecurity;
    }

    @GetMapping
    public List<CareerSkillRequirementResponse> getForListing(@PathVariable OpportunityType type,
                                                               @PathVariable Long listingId) {
        return requirementService.getForListing(type, listingId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('INDUSTRY') and "
            + "@ownershipSecurity.isCareerListingOwner(#type, #listingId, authentication.name))")
    public ResponseEntity<CareerSkillRequirementResponse> add(@PathVariable OpportunityType type,
                                                               @PathVariable Long listingId,
                                                               @Valid @RequestBody SkillRequirementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(requirementService.add(type, listingId, request));
    }

    @PutMapping("/{requirementId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('INDUSTRY') and "
            + "@ownershipSecurity.isCareerListingOwner(#type, #listingId, authentication.name))")
    public CareerSkillRequirementResponse update(@PathVariable OpportunityType type,
                                                 @PathVariable Long listingId,
                                                 @PathVariable Long requirementId,
                                                 @Valid @RequestBody CareerSkillRequirementUpdateRequest request) {
        return requirementService.update(type, listingId, requirementId, request);
    }

    @DeleteMapping("/{requirementId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('INDUSTRY') and "
            + "@ownershipSecurity.isCareerListingOwner(#type, #listingId, authentication.name))")
    public ResponseEntity<Void> delete(@PathVariable OpportunityType type, @PathVariable Long listingId,
                                       @PathVariable Long requirementId) {
        requirementService.delete(type, listingId, requirementId);
        return ResponseEntity.noContent().build();
    }
}