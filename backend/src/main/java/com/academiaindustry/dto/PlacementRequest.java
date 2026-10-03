package com.academiaindustry.dto;

import com.academiaindustry.entity.PlacementStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class PlacementRequest {
    @NotNull @Positive private Long applicationId;
    @NotNull private PlacementStatus status;
    private LocalDate interviewDate;
    @Size(max = 2000) private String notes;
    public PlacementRequest() { }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public PlacementStatus getStatus() { return status; }
    public void setStatus(PlacementStatus status) { this.status = status; }
    public LocalDate getInterviewDate() { return interviewDate; }
    public void setInterviewDate(LocalDate interviewDate) { this.interviewDate = interviewDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}