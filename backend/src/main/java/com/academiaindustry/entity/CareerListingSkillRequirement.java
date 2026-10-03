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
import org.hibernate.annotations.Check;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "career_listing_skill_requirements", uniqueConstraints = {
        @UniqueConstraint(name = "uk_internship_required_skill", columnNames = {"internship_id", "skill_id"}),
        @UniqueConstraint(name = "uk_job_required_skill", columnNames = {"job_id", "skill_id"})
})
@Check(constraints = "(internship_id is not null and job_id is null) or (internship_id is null and job_id is not null)")
public class CareerListingSkillRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Internship internship;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Job job;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProficiencyLevel requiredLevel;

    protected CareerListingSkillRequirement() {
    }

    public CareerListingSkillRequirement(Internship internship, Job job, Skill skill,
                                         ProficiencyLevel requiredLevel) {
        if ((internship == null) == (job == null)) {
            throw new IllegalArgumentException("A skill requirement must belong to exactly one career listing.");
        }
        this.internship = internship;
        this.job = job;
        this.skill = skill;
        this.requiredLevel = requiredLevel;
    }

    public Long getId() { return id; }
    public Internship getInternship() { return internship; }
    public Job getJob() { return job; }
    public Skill getSkill() { return skill; }
    public ProficiencyLevel getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(ProficiencyLevel requiredLevel) { this.requiredLevel = requiredLevel; }
}