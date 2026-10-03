package com.academiaindustry.dto;

import com.academiaindustry.entity.EmploymentType;
import com.academiaindustry.entity.OpportunityStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class JobRequest {

    @NotNull
    @Positive
    private Long industryId;

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 5000)
    private String description;

    @NotBlank
    @Size(max = 200)
    private String location;

    @NotNull
    private EmploymentType employmentType;

    @DecimalMin("0.00")
    @DecimalMax("10.00")
    private BigDecimal minimumCgpa;

    @NotNull
    private LocalDate applicationDeadline;

    @NotNull
    private OpportunityStatus status;

    public JobRequest() {
    }

    public Long getIndustryId() {
        return industryId;
    }

    public void setIndustryId(Long industryId) {
        this.industryId = industryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public BigDecimal getMinimumCgpa() {
        return minimumCgpa;
    }

    public void setMinimumCgpa(BigDecimal minimumCgpa) {
        this.minimumCgpa = minimumCgpa;
    }

    public LocalDate getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(LocalDate applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    public OpportunityStatus getStatus() {
        return status;
    }

    public void setStatus(OpportunityStatus status) {
        this.status = status;
    }
}