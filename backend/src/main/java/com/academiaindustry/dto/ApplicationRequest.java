package com.academiaindustry.dto;

import com.academiaindustry.entity.OpportunityType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ApplicationRequest {

    @NotNull
    @Positive
    private Long studentId;

    @NotNull
    private OpportunityType opportunityType;

    @NotNull
    @Positive
    private Long opportunityId;

    @Size(max = 50)
    private List<@NotNull @Positive Long> sharedPortfolioItemIds = List.of();

    public ApplicationRequest() {
    }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public OpportunityType getOpportunityType() { return opportunityType; }
    public void setOpportunityType(OpportunityType opportunityType) { this.opportunityType = opportunityType; }
    public Long getOpportunityId() { return opportunityId; }
    public void setOpportunityId(Long opportunityId) { this.opportunityId = opportunityId; }
    public List<Long> getSharedPortfolioItemIds() { return sharedPortfolioItemIds; }
    public void setSharedPortfolioItemIds(List<Long> sharedPortfolioItemIds) {
        this.sharedPortfolioItemIds = sharedPortfolioItemIds == null ? List.of() : sharedPortfolioItemIds;
    }
}