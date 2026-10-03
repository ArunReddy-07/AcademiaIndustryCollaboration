package com.academiaindustry.dto;

import com.academiaindustry.entity.ProficiencyLevel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SkillRequirementRequest {

    @NotNull
    @Positive
    private Long skillId;

    @NotNull
    private ProficiencyLevel requiredLevel;

    public SkillRequirementRequest() {
    }

    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }
    public ProficiencyLevel getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(ProficiencyLevel requiredLevel) { this.requiredLevel = requiredLevel; }
}