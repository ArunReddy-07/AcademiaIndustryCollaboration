package com.academiaindustry.dto;

import com.academiaindustry.entity.ApplicationStatus;
import com.academiaindustry.entity.OpportunityType;

import java.time.LocalDateTime;

public class FacultyApplicationSummary {
    private Long id;
    private OpportunityType opportunityType;
    private Long opportunityId;
    private String opportunityTitle;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public OpportunityType getOpportunityType() { return opportunityType; }
    public void setOpportunityType(OpportunityType opportunityType) { this.opportunityType = opportunityType; }
    public Long getOpportunityId() { return opportunityId; }
    public void setOpportunityId(Long opportunityId) { this.opportunityId = opportunityId; }
    public String getOpportunityTitle() { return opportunityTitle; }
    public void setOpportunityTitle(String opportunityTitle) { this.opportunityTitle = opportunityTitle; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }
}
