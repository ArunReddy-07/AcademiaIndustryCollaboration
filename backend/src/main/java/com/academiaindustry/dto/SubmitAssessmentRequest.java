package com.academiaindustry.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SubmitAssessmentRequest(@NotNull List<@Valid AssessmentAnswerRequest> answers) {
}
