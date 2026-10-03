package com.academiaindustry.controller;

import com.academiaindustry.dto.StudentSkillRequest;
import com.academiaindustry.dto.StudentSkillResponse;
import com.academiaindustry.security.OwnershipSecurity;
import com.academiaindustry.service.StudentSkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
@RequestMapping("/api/student-skills")
public class StudentSkillController {

    private final StudentSkillService studentSkillService;
    private final OwnershipSecurity ownershipSecurity;

    public StudentSkillController(StudentSkillService studentSkillService, OwnershipSecurity ownershipSecurity) {
        this.studentSkillService = studentSkillService;
        this.ownershipSecurity = ownershipSecurity;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('STUDENT') and @ownershipSecurity.isStudentOwner(#request.studentId, authentication.name))")
    public ResponseEntity<StudentSkillResponse> create(@Valid @RequestBody StudentSkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentSkillService.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('STUDENT') and @ownershipSecurity.isStudentSkillOwner(#id, authentication.name))")
    public StudentSkillResponse getById(@PathVariable Long id) {
        return studentSkillService.getById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMICIAN')")
    public List<StudentSkillResponse> getAll() {
        return studentSkillService.getAll();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public List<StudentSkillResponse> getMine(Authentication authentication) {
        return studentSkillService.getMine(authentication.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('STUDENT') and @ownershipSecurity.isStudentSkillOwner(#id, authentication.name) "
            + "and @ownershipSecurity.isStudentOwner(#request.studentId, authentication.name))")
    public StudentSkillResponse update(@PathVariable Long id, @Valid @RequestBody StudentSkillRequest request) {
        return studentSkillService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('STUDENT') and @ownershipSecurity.isStudentSkillOwner(#id, authentication.name))")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentSkillService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
