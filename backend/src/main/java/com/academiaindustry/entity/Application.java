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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "applications", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_opportunity", columnNames = {"student_id", "opportunity_type", "opportunity_id"})
})
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "opportunity_type", nullable = false, length = 30)
    private OpportunityType opportunityType;

    @NotNull
    @Column(name = "opportunity_id", nullable = false)
    private Long opportunityId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "application_shared_portfolio_items",
            joinColumns = @JoinColumn(name = "application_id"),
            inverseJoinColumns = @JoinColumn(name = "portfolio_item_id"))
    private Set<StudentPortfolioItem> sharedPortfolioItems = new LinkedHashSet<>();

    protected Application() {
    }

    public Application(Student student, OpportunityType opportunityType, Long opportunityId,
                       ApplicationStatus status) {
        this.student = student;
        this.opportunityType = opportunityType;
        this.opportunityId = opportunityId;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        appliedAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public OpportunityType getOpportunityType() {
        return opportunityType;
    }

    public void setOpportunityType(OpportunityType opportunityType) {
        this.opportunityType = opportunityType;
    }

    public Long getOpportunityId() {
        return opportunityId;
    }

    public void setOpportunityId(Long opportunityId) {
        this.opportunityId = opportunityId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Set<StudentPortfolioItem> getSharedPortfolioItems() {
        return sharedPortfolioItems;
    }

    public void setSharedPortfolioItems(Set<StudentPortfolioItem> sharedPortfolioItems) {
        this.sharedPortfolioItems = sharedPortfolioItems == null ? new LinkedHashSet<>() : sharedPortfolioItems;
    }
}