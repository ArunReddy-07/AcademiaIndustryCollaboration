package com.academiaindustry.dto;

import java.time.Instant;
import java.util.List;

public class ResumeAnalysisResponse {

    private Long id;
    private String fileName;
    private Instant uploadedAt;
    private Integer overallScore;
    private String summary;
    private Integer profileCompleteness;
    private List<String> missingSections;
    private List<String> weakContentSignals;
    private List<String> strengths;
    private List<String> extractedSkills;
    private List<String> skillGapSkills;
    private List<ResumeCategoryScore> categoryBreakdown;

    public ResumeAnalysisResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Integer getProfileCompleteness() {
        return profileCompleteness;
    }

    public void setProfileCompleteness(Integer profileCompleteness) {
        this.profileCompleteness = profileCompleteness;
    }

    public List<String> getMissingSections() {
        return missingSections;
    }

    public void setMissingSections(List<String> missingSections) {
        this.missingSections = missingSections;
    }

    public List<String> getWeakContentSignals() {
        return weakContentSignals;
    }

    public void setWeakContentSignals(List<String> weakContentSignals) {
        this.weakContentSignals = weakContentSignals;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getExtractedSkills() {
        return extractedSkills;
    }

    public void setExtractedSkills(List<String> extractedSkills) {
        this.extractedSkills = extractedSkills;
    }

    public List<String> getSkillGapSkills() {
        return skillGapSkills;
    }

    public void setSkillGapSkills(List<String> skillGapSkills) {
        this.skillGapSkills = skillGapSkills;
    }

    public List<ResumeCategoryScore> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public void setCategoryBreakdown(List<ResumeCategoryScore> categoryBreakdown) {
        this.categoryBreakdown = categoryBreakdown;
    }

    public static class ResumeCategoryScore {
        private String name;
        private Integer score;
        private Integer weight;
        private String description;

        public ResumeCategoryScore() {
        }

        public ResumeCategoryScore(String name, Integer score, Integer weight, String description) {
            this.name = name;
            this.score = score;
            this.weight = weight;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public Integer getWeight() {
            return weight;
        }

        public void setWeight(Integer weight) {
            this.weight = weight;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
