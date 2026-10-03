package com.academiaindustry.controller;

import com.academiaindustry.dto.CollaborationRequest;
import com.academiaindustry.dto.CollaborationResponse;
import com.academiaindustry.dto.CollaborationStatusRequest;
import com.academiaindustry.service.CollaborationService;
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

import java.util.List;

@RestController
@RequestMapping("/api/collaborations")
public class CollaborationController {

    private final CollaborationService collaborationService;

    public CollaborationController(CollaborationService collaborationService) {
        this.collaborationService = collaborationService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'ACADEMICIAN', 'INDUSTRY', 'STUDENT', 'INSTITUTION')")
    public List<CollaborationResponse> getAll(Authentication authentication) {
        return collaborationService.getAll(authentication.getName(), isAdministrator(authentication));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'ACADEMICIAN', 'INDUSTRY', 'STUDENT', 'INSTITUTION')")
    public CollaborationResponse getById(Authentication authentication, @PathVariable Long id) {
        return collaborationService.getById(id, authentication.getName(), isAdministrator(authentication));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INDUSTRY', 'FACULTY', 'ACADEMICIAN', 'INSTITUTION')")
    public ResponseEntity<CollaborationResponse> create(Authentication authentication,
                                                        @Valid @RequestBody CollaborationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(collaborationService.create(
                request, authentication.getName(), isAdministrator(authentication)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'INDUSTRY', 'FACULTY', 'ACADEMICIAN', 'INSTITUTION')")
    public CollaborationResponse updateStatus(Authentication authentication, @PathVariable Long id,
                                               @Valid @RequestBody CollaborationStatusRequest request) {
        return collaborationService.updateStatus(id, request.getStatus(), authentication.getName(),
                isAdministrator(authentication));
    }

    private boolean isAdministrator(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}