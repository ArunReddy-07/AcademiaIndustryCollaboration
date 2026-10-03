package com.academiaindustry.dto;

import com.academiaindustry.entity.ProficiencyLevel;
import jakarta.validation.constraints.NotNull;

public class CareerSkillRequirementUpdateRequest {

    @NotNull
    private ProficiencyLevel requiredLevel;

    public ProficiencyLevel getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(ProficiencyLevel requiredLevel) { this.requiredLevel = requiredLevel; }
}