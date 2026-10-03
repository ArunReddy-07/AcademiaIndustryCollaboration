package com.academiaindustry.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SkillAssessmentRequest {

    @NotNull
    @Positive
    private Long studentId;

    @NotNull
    @Positive
    private Long skillId;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer score;

    public SkillAssessmentRequest() {
    }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
}