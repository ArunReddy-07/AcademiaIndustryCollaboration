package com.academiaindustry.dto;

import com.academiaindustry.entity.OpportunityType;
import java.util.List;

public class SkillMatchResponse {

    private Long opportunityId;
    private OpportunityType opportunityType;
    private Long studentId;
    private int matchedCount;
    private int requiredCount;
    private int matchPercentage;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private List<String> insufficientProficiencySkills;

    public SkillMatchResponse() {
    }

    public Long getOpportunityId() { return opportunityId; }
    public void setOpportunityId(Long opportunityId) { this.opportunityId = opportunityId; }
    public OpportunityType getOpportunityType() { return opportunityType; }
    public void setOpportunityType(OpportunityType opportunityType) { this.opportunityType = opportunityType; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public int getMatchedCount() { return matchedCount; }
    public void setMatchedCount(int matchedCount) { this.matchedCount = matchedCount; }
    public int getRequiredCount() { return requiredCount; }
    public void setRequiredCount(int requiredCount) { this.requiredCount = requiredCount; }
    public int getMatchPercentage() { return matchPercentage; }
    public void setMatchPercentage(int matchPercentage) { this.matchPercentage = matchPercentage; }
    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }
    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }
    public List<String> getInsufficientProficiencySkills() { return insufficientProficiencySkills; }
    public void setInsufficientProficiencySkills(List<String> insufficientProficiencySkills) {
        this.insufficientProficiencySkills = insufficientProficiencySkills;
    }
}