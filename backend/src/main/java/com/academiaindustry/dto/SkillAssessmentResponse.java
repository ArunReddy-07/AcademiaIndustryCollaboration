package com.academiaindustry.dto;

import com.academiaindustry.entity.ProficiencyLevel;

import java.time.LocalDateTime;

public class SkillAssessmentResponse {

    private Long id;
    private Long studentId;
    private Long skillId;
    private Integer score;
    private ProficiencyLevel level;
    private LocalDateTime assessedAt;

    public SkillAssessmentResponse() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public ProficiencyLevel getLevel() { return level; }
    public void setLevel(ProficiencyLevel level) { this.level = level; }
    public LocalDateTime getAssessedAt() { return assessedAt; }
    public void setAssessedAt(LocalDateTime assessedAt) { this.assessedAt = assessedAt; }
}