package com.academiaindustry.dto;

import com.academiaindustry.entity.ApplicationStatus;
import com.academiaindustry.entity.OpportunityType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ApplicationResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String institutionName;
    private String branch;
    private Integer graduationYear;
    private BigDecimal cgpa;
    private OpportunityType opportunityType;
    private Long opportunityId;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
    private List<StudentPortfolioItemResponse> sharedPortfolioItems = List.of();

    public ApplicationResponse() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }
    public BigDecimal getCgpa() { return cgpa; }
    public void setCgpa(BigDecimal cgpa) { this.cgpa = cgpa; }
    public OpportunityType getOpportunityType() { return opportunityType; }
    public void setOpportunityType(OpportunityType opportunityType) { this.opportunityType = opportunityType; }
    public Long getOpportunityId() { return opportunityId; }
    public void setOpportunityId(Long opportunityId) { this.opportunityId = opportunityId; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<StudentPortfolioItemResponse> getSharedPortfolioItems() { return sharedPortfolioItems; }
    public void setSharedPortfolioItems(List<StudentPortfolioItemResponse> sharedPortfolioItems) {
        this.sharedPortfolioItems = sharedPortfolioItems;
    }
}