package com.academiaindustry.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "skill_assessments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_skill_assessment", columnNames = {"student_id", "skill_id"})
})
public class SkillAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @NotNull
    @Min(0)
    @Max(100)
    @Column(nullable = false)
    private Integer score;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProficiencyLevel level;

    @Column(nullable = false, updatable = false)
    private LocalDateTime assessedAt;

    protected SkillAssessment() {
    }

    public SkillAssessment(Student student, Skill skill, Integer score, ProficiencyLevel level) {
        this.student = student;
        this.skill = skill;
        this.score = score;
        this.level = level;
    }

    @PrePersist
    protected void onCreate() {
        assessedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public Skill getSkill() { return skill; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public ProficiencyLevel getLevel() { return level; }
    public void setLevel(ProficiencyLevel level) { this.level = level; }
    public LocalDateTime getAssessedAt() { return assessedAt; }
}