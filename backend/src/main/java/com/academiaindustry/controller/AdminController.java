package com.academiaindustry.controller;

import com.academiaindustry.dto.ApplicationResponse;
import com.academiaindustry.dto.ApplicationStatusRequest;
import com.academiaindustry.dto.CollaborationStatusRequest;
import com.academiaindustry.dto.CollaborationResponse;
import com.academiaindustry.dto.InstitutionRequest;
import com.academiaindustry.dto.InstitutionResponse;
import com.academiaindustry.dto.OpportunityRequest;
import com.academiaindustry.dto.OpportunityResponse;
import com.academiaindustry.dto.PlacementRequest;
import com.academiaindustry.dto.PlacementResponse;
import com.academiaindustry.dto.SkillRequest;
import com.academiaindustry.dto.SkillResponse;
import com.academiaindustry.dto.UserRequest;
import com.academiaindustry.dto.UserResponse;
import com.academiaindustry.service.ApplicationService;
import com.academiaindustry.service.CollaborationService;
import com.academiaindustry.service.InstitutionService;
import com.academiaindustry.service.OpportunityService;
import com.academiaindustry.service.PlacementService;
import com.academiaindustry.service.SkillService;
import com.academiaindustry.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final SkillService skillService;
    private final InstitutionService institutionService;
    private final OpportunityService opportunityService;
    private final ApplicationService applicationService;
    private final CollaborationService collaborationService;
    private final PlacementService placementService;

    public AdminController(UserService userService, SkillService skillService,
                           InstitutionService institutionService, OpportunityService opportunityService,
                           ApplicationService applicationService, CollaborationService collaborationService,
                           PlacementService placementService) {
        this.userService = userService;
        this.skillService = skillService;
        this.institutionService = institutionService;
        this.opportunityService = opportunityService;
        this.applicationService = applicationService;
        this.collaborationService = collaborationService;
        this.placementService = placementService;
    }

    @GetMapping("/users")
    public List<UserResponse> users() { return userService.getAll(); }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @PutMapping("/users/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/skills")
    public List<SkillResponse> skills() { return skillService.getAll(); }

    @PostMapping("/skills")
    public ResponseEntity<SkillResponse> createSkill(@Valid @RequestBody SkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(skillService.create(request));
    }

    @PutMapping("/skills/{id}")
    public SkillResponse updateSkill(@PathVariable Long id, @Valid @RequestBody SkillRequest request) {
        return skillService.update(id, request);
    }

    @DeleteMapping("/skills/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        skillService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/institutions")
    public List<InstitutionResponse> institutions() { return institutionService.getAll(); }

    @PostMapping("/institutions")
    public ResponseEntity<InstitutionResponse> createInstitution(@Valid @RequestBody InstitutionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(institutionService.create(request));
    }

    @PutMapping("/institutions/{id}")
    public InstitutionResponse updateInstitution(@PathVariable Long id,
                                                 @Valid @RequestBody InstitutionRequest request) {
        return institutionService.update(id, request);
    }

    @DeleteMapping("/institutions/{id}")
    public ResponseEntity<Void> deleteInstitution(@PathVariable Long id) {
        institutionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/opportunities")
    public List<OpportunityResponse> opportunities() { return opportunityService.getAll(); }

    @PostMapping("/opportunities")
    public ResponseEntity<OpportunityResponse> createOpportunity(@Valid @RequestBody OpportunityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(opportunityService.create(request));
    }

    @PutMapping("/opportunities/{id}")
    public OpportunityResponse updateOpportunity(@PathVariable Long id,
                                                 @Valid @RequestBody OpportunityRequest request) {
        return opportunityService.update(id, request);
    }

    @DeleteMapping("/opportunities/{id}")
    public ResponseEntity<Void> deleteOpportunity(@PathVariable Long id) {
        opportunityService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/applications")
    public List<ApplicationResponse> applications() { return applicationService.getAll(); }

    @PatchMapping("/applications/{id}/status")
    public ApplicationResponse updateApplicationStatus(@PathVariable Long id,
                                                       @Valid @RequestBody ApplicationStatusRequest request) {
        return applicationService.updateStatus(id, request.getStatus(), "admin", true);
    }

    @GetMapping("/collaborations")
    public List<CollaborationResponse> collaborations() {
        return collaborationService.getAll("admin", true);
    }

    @PatchMapping("/collaborations/{id}/status")
    public CollaborationResponse updateCollaborationStatus(@PathVariable Long id,
                                                           @Valid @RequestBody CollaborationStatusRequest request) {
        return collaborationService.updateStatus(id, request.getStatus(), "admin", true);
    }

    @GetMapping("/placements")
    public List<PlacementResponse> placements() { return placementService.getAll(); }

    @PutMapping("/placements/{id}")
    public PlacementResponse updatePlacement(@PathVariable Long id,
                                             @Valid @RequestBody PlacementRequest request) {
        return placementService.update(id, request);
    }
}