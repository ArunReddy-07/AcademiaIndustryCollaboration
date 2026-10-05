package com.academiaindustry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssessmentAnswerRequest(
        @NotNull Long questionId,
        @NotBlank @Size(max = 300) String selectedAnswer) {
}
