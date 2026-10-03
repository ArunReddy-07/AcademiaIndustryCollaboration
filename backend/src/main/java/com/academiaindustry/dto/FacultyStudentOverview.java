package com.academiaindustry.dto;

import java.util.List;

public class FacultyStudentOverview {
    private StudentResponse student;
    private List<StudentSkillResponse> skills;
    private List<FacultyApplicationSummary> applications;

    public StudentResponse getStudent() { return student; }
    public void setStudent(StudentResponse student) { this.student = student; }
    public List<StudentSkillResponse> getSkills() { return skills; }
    public void setSkills(List<StudentSkillResponse> skills) { this.skills = skills; }
    public List<FacultyApplicationSummary> getApplications() { return applications; }
    public void setApplications(List<FacultyApplicationSummary> applications) { this.applications = applications; }
}
