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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "opportunity_skill_requirements", uniqueConstraints = {
        @UniqueConstraint(name = "uk_opportunity_required_skill", columnNames = {"opportunity_id", "skill_id"})
})
public class OpportunitySkillRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProficiencyLevel requiredLevel;

    protected OpportunitySkillRequirement() {
    }

    public OpportunitySkillRequirement(Opportunity opportunity, Skill skill, ProficiencyLevel requiredLevel) {
        this.opportunity = opportunity;
        this.skill = skill;
        this.requiredLevel = requiredLevel;
    }

    public Long getId() { return id; }
    public Opportunity getOpportunity() { return opportunity; }
    public Skill getSkill() { return skill; }
    public ProficiencyLevel getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(ProficiencyLevel requiredLevel) { this.requiredLevel = requiredLevel; }
}