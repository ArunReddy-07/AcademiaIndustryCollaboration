package com.academiaindustry.controller;

import com.academiaindustry.dto.StudentRequest;
import com.academiaindustry.dto.StudentResponse;
import com.academiaindustry.service.StudentService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'FACULTY', 'ACADEMICIAN')")
    public StudentResponse getCurrentStudent(Authentication authentication) {
        return studentService.getByUserEmail(authentication.getName());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'ACADEMICIAN', 'INDUSTRY', 'INSTITUTION')")
    public List<StudentResponse> getAll(Authentication authentication,
                                        @RequestParam(required = false) Long skillId) {
        boolean institution = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_INSTITUTION"));
        if (institution) return studentService.getForInstitution(authentication.getName(), skillId);
        boolean faculty = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_FACULTY"));
        if (faculty) return studentService.getForFaculty(authentication.getName(), skillId);
        return skillId == null ? studentService.getAll() : studentService.getAllBySkill(skillId);
    }

    @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMICIAN', 'INDUSTRY') or "
            + "(hasRole('FACULTY') and @ownershipSecurity.isFacultyForStudent(#id, authentication.name)) or "
            + "(hasRole('INSTITUTION') and @ownershipSecurity.isStudentInstitutionOwner(#id, authentication.name)) or "
            + "(hasRole('STUDENT') and @ownershipSecurity.isStudentOwner(#id, authentication.name))")
    public StudentResponse getById(@PathVariable Long id) {
        return studentService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMICIAN') or "
            + "(hasRole('FACULTY') and @ownershipSecurity.isFacultyForStudent(#id, authentication.name)) or "
            + "(hasRole('STUDENT') and @ownershipSecurity.isUser(authentication.name, #request.userId)) or "
            + "(hasRole('INSTITUTION') and @ownershipSecurity.isInstitutionOwner(authentication.name, #request.institutionId))")
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMICIAN') or "
            + "(hasRole('FACULTY') and @ownershipSecurity.isFacultyForStudent(#id, authentication.name)) or "
            + "(hasRole('STUDENT') and @ownershipSecurity.isStudentOwner(#id, authentication.name)) or "
            + "(hasRole('INSTITUTION') and @ownershipSecurity.isStudentInstitutionOwner(#id, authentication.name) "
            + "and @ownershipSecurity.isInstitutionOwner(authentication.name, #request.institutionId))")
    public StudentResponse update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return studentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'ACADEMICIAN') or "
            + "(hasRole('STUDENT') and @ownershipSecurity.isStudentOwner(#id, authentication.name)) or "
            + "(hasRole('INSTITUTION') and @ownershipSecurity.isStudentInstitutionOwner(#id, authentication.name))")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}