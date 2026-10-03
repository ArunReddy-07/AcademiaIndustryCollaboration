package com.academiaindustry.dto;

import com.academiaindustry.entity.OpportunityType;
import com.academiaindustry.entity.ProficiencyLevel;

public class CareerSkillRequirementResponse {

    private Long id;
    private OpportunityType listingType;
    private Long listingId;
    private Long skillId;
    private String skillName;
    private ProficiencyLevel requiredLevel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public OpportunityType getListingType() { return listingType; }
    public void setListingType(OpportunityType listingType) { this.listingType = listingType; }
    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }
    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    public ProficiencyLevel getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(ProficiencyLevel requiredLevel) { this.requiredLevel = requiredLevel; }
}