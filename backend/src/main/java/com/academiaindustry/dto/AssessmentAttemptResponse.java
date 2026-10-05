package com.academiaindustry.dto;

import java.time.Instant;
import java.util.List;

public record AssessmentAttemptResponse(
        Long attemptId,
        String skill,
        String difficulty,
        int numberOfQuestions,
        List<AssessmentQuestionResponse> questions,
        boolean submitted,
        Integer score,
        Integer correctCount,
        List<AssessmentTopicPerformance> topicPerformance,
        List<String> strongAreas,
        List<String> needsImprovement,
        List<String> skillGaps,
        List<String> recommendations,
        Instant createdAt,
        Instant submittedAt) {
}
