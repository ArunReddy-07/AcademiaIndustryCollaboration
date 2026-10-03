package com.academiaindustry.dto;

import com.academiaindustry.entity.OpportunityStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InternshipRequest {

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

    @NotBlank
    @Size(max = 100)
    private String duration;

    @DecimalMin("0.00")
    private BigDecimal stipend;

    @NotNull
    private LocalDate applicationDeadline;

    @NotNull
    private OpportunityStatus status;

    public InternshipRequest() {
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public BigDecimal getStipend() {
        return stipend;
    }

    public void setStipend(BigDecimal stipend) {
        this.stipend = stipend;
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