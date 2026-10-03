package com.academiaindustry.dto;

import com.academiaindustry.entity.PlacementStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PlacementResponse {
    private Long id;
    private Long applicationId;
    private Long studentId;
    private PlacementStatus status;
    private LocalDate interviewDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public PlacementResponse() { }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public PlacementStatus getStatus() { return status; }
    public void setStatus(PlacementStatus status) { this.status = status; }
    public LocalDate getInterviewDate() { return interviewDate; }
    public void setInterviewDate(LocalDate interviewDate) { this.interviewDate = interviewDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}