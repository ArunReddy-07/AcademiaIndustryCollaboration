package com.academiaindustry.dto;

import com.academiaindustry.entity.CollaborationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CollaborationRequest {

    @NotNull @Positive private Long industryId;
    @NotNull @Positive private Long institutionId;
    @Positive private Long academicianId;
    @Positive private Long studentId;
    @NotBlank @Size(max = 200) private String title;
    @NotBlank @Size(max = 5000) private String description;
    @NotNull private CollaborationStatus status;

    public CollaborationRequest() { }
    public Long getIndustryId() { return industryId; }
    public void setIndustryId(Long industryId) { this.industryId = industryId; }
    public Long getInstitutionId() { return institutionId; }
    public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }
    public Long getAcademicianId() { return academicianId; }
    public void setAcademicianId(Long academicianId) { this.academicianId = academicianId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public CollaborationStatus getStatus() { return status; }
    public void setStatus(CollaborationStatus status) { this.status = status; }
}