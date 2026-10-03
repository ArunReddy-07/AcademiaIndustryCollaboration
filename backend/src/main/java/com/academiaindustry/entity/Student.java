package com.academiaindustry.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "student_profiles")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String branch;

    @NotNull
    @Min(1900)
    @Max(2100)
    @Column(nullable = false)
    private Integer graduationYear;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("10.00")
    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal cgpa;

    @Size(max = 500)
    @Column(length = 500)
    private String careerGoal;

    @Size(max = 2000)
    @Column(length = 2000)
    private String bio;

    @NotNull
    @Min(0)
    @Max(100)
    @Column(nullable = false)
    private Integer profileCompletionPercentage = 0;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Student() {
    }

    public Student(User user, Institution institution, String branch, Integer graduationYear,
                   BigDecimal cgpa, String careerGoal, String bio) {
        this.user = user;
        this.institution = institution;
        this.branch = branch;
        this.graduationYear = graduationYear;
        this.cgpa = cgpa;
        this.careerGoal = careerGoal;
        this.bio = bio;
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

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(Integer graduationYear) {
        this.graduationYear = graduationYear;
    }

    public BigDecimal getCgpa() {
        return cgpa;
    }

    public void setCgpa(BigDecimal cgpa) {
        this.cgpa = cgpa;
    }

    public String getCareerGoal() {
        return careerGoal;
    }

    public void setCareerGoal(String careerGoal) {
        this.careerGoal = careerGoal;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Integer getProfileCompletionPercentage() {
        return profileCompletionPercentage;
    }

    public void setProfileCompletionPercentage(Integer profileCompletionPercentage) {
        this.profileCompletionPercentage = profileCompletionPercentage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}