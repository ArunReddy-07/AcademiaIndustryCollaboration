package com.academiaindustry.controller;

import com.academiaindustry.dto.InstitutionRequest;
import com.academiaindustry.dto.InstitutionResponse;
import com.academiaindustry.service.InstitutionService;
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
@RequestMapping("/api/institutions")
@PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'ACADEMICIAN', 'INDUSTRY', 'STUDENT', 'INSTITUTION')")
public class InstitutionController {

    private final InstitutionService institutionService;

    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @GetMapping
    public List<InstitutionResponse> getAll() {
        return institutionService.getAll();
    }

    @PostMapping("/resolve")
    public InstitutionResponse resolveOrCreate(@Valid @RequestBody InstitutionRequest request) {
        return institutionService.findOrCreateByName(request.getName());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('INSTITUTION')")
    public InstitutionResponse getCurrent(Authentication authentication) {
        return institutionService.getForAccount(authentication.getName());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('INSTITUTION')")
    public InstitutionResponse updateCurrent(Authentication authentication,
                                             @Valid @RequestBody InstitutionRequest request) {
        return institutionService.updateForAccount(authentication.getName(), request);
    }

    @GetMapping("/{id}")
    public InstitutionResponse getById(@PathVariable Long id) {
        return institutionService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'ACADEMICIAN')")
    public ResponseEntity<InstitutionResponse> create(@Valid @RequestBody InstitutionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(institutionService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public InstitutionResponse update(@PathVariable Long id, @Valid @RequestBody InstitutionRequest request) {
        return institutionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        institutionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}