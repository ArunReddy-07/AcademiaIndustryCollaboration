package com.academiaindustry.dto;

import java.util.List;

public record AssessmentQuestionResponse(
        Long questionId,
        String question,
        List<String> options,
        String topic,
        String difficulty) {
}
