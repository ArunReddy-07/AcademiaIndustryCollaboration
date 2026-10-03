package com.academiaindustry.controller;

import com.academiaindustry.dto.InternshipRequest;
import com.academiaindustry.dto.InternshipResponse;
import com.academiaindustry.service.InternshipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/internships")
public class InternshipController {

    private final InternshipService internshipService;

    public InternshipController(InternshipService internshipService) {
        this.internshipService = internshipService;
    }

    @GetMapping
    public List<InternshipResponse> getAll() {
        return internshipService.getAll();
    }

    @GetMapping("/{id}")
    public InternshipResponse getById(@PathVariable Long id) {
        return internshipService.getById(id);
    }

    @PostMapping
        @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('INDUSTRY') and @ownershipSecurity.isUser(authentication.name, #request.industryId))")
    public ResponseEntity<InternshipResponse> create(@Valid @RequestBody InternshipRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(internshipService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('INDUSTRY') and @ownershipSecurity.isInternshipOwner(#id, authentication.name))")
    public InternshipResponse update(@PathVariable Long id, @Valid @RequestBody InternshipRequest request) {
        return internshipService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('INDUSTRY') and @ownershipSecurity.isInternshipOwner(#id, authentication.name))")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        internshipService.delete(id);
        return ResponseEntity.noContent().build();
    }
}