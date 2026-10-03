package com.academiaindustry.dto;

import com.academiaindustry.entity.ProficiencyLevel;

public class SkillRequirementResponse {

    private Long id;
    private Long opportunityId;
    private Long skillId;
    private String skillName;
    private ProficiencyLevel requiredLevel;

    public SkillRequirementResponse() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOpportunityId() { return opportunityId; }
    public void setOpportunityId(Long opportunityId) { this.opportunityId = opportunityId; }
    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    public ProficiencyLevel getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(ProficiencyLevel requiredLevel) { this.requiredLevel = requiredLevel; }
}