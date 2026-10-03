package com.academiaindustry.dto;

import com.academiaindustry.entity.CollaborationStatus;

import java.time.LocalDateTime;

public class CollaborationResponse {
    private Long id;
    private Long industryId;
    private Long institutionId;
    private Long academicianId;
    private Long studentId;
    private String title;
    private String description;
    private CollaborationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public CollaborationResponse() { }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}