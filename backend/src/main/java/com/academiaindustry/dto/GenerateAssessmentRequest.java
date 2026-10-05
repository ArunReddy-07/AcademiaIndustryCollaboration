package com.academiaindustry.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class GenerateAssessmentRequest {

    @NotBlank(message = "Enter a skill or topic.")
    @Size(max = 100, message = "Skill or topic must be 100 characters or fewer.")
    @Pattern(regexp = "[\\p{L}\\p{N}][\\p{L}\\p{N}+#./()&,: _-]*",
            message = "Use a valid technical skill or topic.")
    private String skill;

    @NotBlank
    @Pattern(regexp = "(?i)Easy|Medium|Hard", message = "Difficulty must be Easy, Medium, or Hard.")
    private String difficulty = "Medium";

    @NotNull(message = "Choose 5, 10, 15, or 20 questions.")
    @Min(value = 5, message = "Choose 5, 10, 15, or 20 questions.")
    @Max(value = 20, message = "Choose 5, 10, 15, or 20 questions.")
    private Integer numberOfQuestions = 10;

    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public Integer getNumberOfQuestions() { return numberOfQuestions; }
    public void setNumberOfQuestions(Integer numberOfQuestions) { this.numberOfQuestions = numberOfQuestions; }
}
