package com.academiaindustry.controller;

import com.academiaindustry.dto.SkillRequest;
import com.academiaindustry.dto.SkillResponse;
import com.academiaindustry.dto.StudentSkillCatalogRequest;
import com.academiaindustry.service.SkillService;
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
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    public List<SkillResponse> getAll() {
        return skillService.getAll();
    }

    @GetMapping("/{id}")
    public SkillResponse getById(@PathVariable Long id) {
        return skillService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'ACADEMICIAN', 'INDUSTRY')")
    public ResponseEntity<SkillResponse> create(@Valid @RequestBody SkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(skillService.create(request));
    }

    @PostMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SkillResponse> createForStudent(
            @Valid @RequestBody StudentSkillCatalogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(skillService.createForStudent(request.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'ACADEMICIAN')")
    public SkillResponse update(@PathVariable Long id, @Valid @RequestBody SkillRequest request) {
        return skillService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        skillService.delete(id);
        return ResponseEntity.noContent().build();
    }
}