package com.academiaindustry.controller;

import com.academiaindustry.dto.FacultyStudentOverview;
import com.academiaindustry.service.FacultyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/faculty")
@PreAuthorize("hasRole('FACULTY')")
public class FacultyController {
    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping("/students")
    public List<FacultyStudentOverview> getStudents(Authentication authentication) {
        return facultyService.getStudents(authentication.getName());
    }
}
