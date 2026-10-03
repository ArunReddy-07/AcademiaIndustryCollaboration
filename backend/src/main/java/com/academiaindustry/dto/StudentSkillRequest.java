package com.academiaindustry.dto;

import com.academiaindustry.entity.ProficiencyLevel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class StudentSkillRequest {

    @NotNull
    @Positive
    private Long studentId;

    @NotNull
    @Positive
    private Long skillId;

    @NotNull
    private ProficiencyLevel proficiencyLevel;

    public StudentSkillRequest() {
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getSkillId() {
        return skillId;
    }

    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }

    public ProficiencyLevel getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(ProficiencyLevel proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }
}