package com.academiaindustry.dto;

public record AssessmentTopicPerformance(
        String topic,
        int correctCount,
        int questionCount,
        int percentage) {
}
