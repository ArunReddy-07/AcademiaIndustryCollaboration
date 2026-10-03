package com.academiaindustry.service;

import com.academiaindustry.dto.FacultyStudentOverview;

import java.util.List;

public interface FacultyService {
    List<FacultyStudentOverview> getStudents(String facultyEmail);
}
