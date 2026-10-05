package com.academiaindustry.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Entity
@Table(name = "assessment_attempts")
public class AssessmentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String skill;

    @NotBlank
    @Column(nullable = false, length = 10)
    private String difficulty;

    @NotNull
    @Column(nullable = false)
    private Integer numberOfQuestions;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String generatedQuestionsJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String studentAnswersJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String resultJson;

    @Column
    private Integer correctCount;

    @Column
    private Integer score;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Instant submittedAt;

    protected AssessmentAttempt() {
    }

    public AssessmentAttempt(Student student, String skill, String difficulty, Integer numberOfQuestions,
                             String generatedQuestionsJson) {
        this.student = student;
        this.skill = skill;
        this.difficulty = difficulty;
        this.numberOfQuestions = numberOfQuestions;
        this.generatedQuestionsJson = generatedQuestionsJson;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public String getSkill() { return skill; }
    public String getDifficulty() { return difficulty; }
    public Integer getNumberOfQuestions() { return numberOfQuestions; }
    public String getGeneratedQuestionsJson() { return generatedQuestionsJson; }
    public String getStudentAnswersJson() { return studentAnswersJson; }
    public void setStudentAnswersJson(String studentAnswersJson) { this.studentAnswersJson = studentAnswersJson; }
    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }
    public Integer getCorrectCount() { return correctCount; }
    public void setCorrectCount(Integer correctCount) { this.correctCount = correctCount; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
}
