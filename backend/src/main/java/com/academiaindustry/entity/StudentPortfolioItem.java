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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "student_portfolio_items")
public class StudentPortfolioItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PortfolioItemType type;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @Size(max = 2000)
    @Column(length = 2000)
    private String description;

    @Size(max = 200)
    @Column(length = 200)
    private String organization;

    @Size(max = 1000)
    @Column(length = 1000)
    private String referenceUrl;

    private LocalDate completedOn;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected StudentPortfolioItem() {
    }

    public StudentPortfolioItem(Student student, PortfolioItemType type, String title,
                                 String description, String organization,
                                 String referenceUrl, LocalDate completedOn) {
        this.student = student;
        this.type = type;
        this.title = title;
        this.description = description;
        this.organization = organization;
        this.referenceUrl = referenceUrl;
        this.completedOn = completedOn;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public PortfolioItemType getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getOrganization() { return organization; }
    public String getReferenceUrl() { return referenceUrl; }
    public LocalDate getCompletedOn() { return completedOn; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
