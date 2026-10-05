package com.academiaindustry.service;

import com.academiaindustry.dto.AssessmentAnswerRequest;
import com.academiaindustry.dto.AssessmentAttemptResponse;
import com.academiaindustry.dto.AssessmentQuestionResponse;
import com.academiaindustry.dto.AssessmentTopicPerformance;
import com.academiaindustry.dto.GenerateAssessmentRequest;
import com.academiaindustry.dto.SubmitAssessmentRequest;
import com.academiaindustry.entity.AssessmentAttempt;
import com.academiaindustry.entity.Student;
import com.academiaindustry.exception.GeminiIntegrationException;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.AssessmentAttemptRepository;
import com.academiaindustry.repository.StudentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AssessmentAttemptService {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentAttemptService.class);
    private static final Set<Integer> ALLOWED_QUESTION_COUNTS = Set.of(5, 10, 15, 20);
    private static final Set<String> VALID_DIFFICULTIES = Set.of("easy", "medium", "hard");
    private static final TypeReference<List<StoredQuestion>> STORED_QUESTION_LIST = new TypeReference<>() { };
    private final AssessmentAttemptRepository attemptRepository;
    private final StudentRepository studentRepository;
    private final GeminiIntegrationService geminiIntegrationService;
    private final ObjectMapper objectMapper;

    public AssessmentAttemptService(AssessmentAttemptRepository attemptRepository,
                                   StudentRepository studentRepository,
                                   GeminiIntegrationService geminiIntegrationService,
                                   ObjectMapper objectMapper) {
        this.attemptRepository = attemptRepository;
        this.studentRepository = studentRepository;
        this.geminiIntegrationService = geminiIntegrationService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AssessmentAttemptResponse generate(String studentEmail, GenerateAssessmentRequest request) {
        String skill = request.getSkill().trim();
        String difficulty = request.getDifficulty().trim();
        int questionCount = request.getNumberOfQuestions();
        if (skill.isBlank() || skill.length() > 100 || !containsLetterOrDigit(skill)) {
            throw new IllegalArgumentException("Enter a valid skill or technical topic (100 characters maximum).");
        }
        if (!VALID_DIFFICULTIES.contains(difficulty.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Difficulty must be Easy, Medium, or Hard.");
        }
        if (!ALLOWED_QUESTION_COUNTS.contains(questionCount)) {
            throw new IllegalArgumentException("Choose 5, 10, 15, or 20 questions.");
        }
        difficulty = capitalize(difficulty);

        Student student = studentRepository.findByUserEmailIgnoreCase(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));

        String generatedContent = geminiIntegrationService.generateAssessmentQuestions(skill, difficulty, questionCount);
        List<StoredQuestion> questions = parseAndValidateQuestions(
                generatedContent, skill, difficulty, questionCount);
        String questionsJson = writeJson(questions, "Generated questions could not be saved.");
        AssessmentAttempt attempt = attemptRepository.save(new AssessmentAttempt(
                student, skill, difficulty, questionCount, questionsJson));
        return toResponse(attempt, questions, null);
    }

    @Transactional(readOnly = true)
    public AssessmentAttemptResponse getAttempt(String studentEmail, Long attemptId) {
        AssessmentAttempt attempt = attemptRepository.findByIdAndStudentEmailIgnoreCase(attemptId, studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment attempt not found."));
        return toResponse(attempt, readQuestions(attempt), readResult(attempt));
    }

    @Transactional
    public AssessmentAttemptResponse submit(String studentEmail, Long attemptId, SubmitAssessmentRequest request) {
        AssessmentAttempt attempt = attemptRepository
                .findByIdAndStudentEmailIgnoreCaseForUpdate(attemptId, studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment attempt not found."));

        List<StoredQuestion> questions = readQuestions(attempt);
        if (attempt.getSubmittedAt() != null) {
            return toResponse(attempt, questions, readResult(attempt));
        }

        Map<Long, String> submittedAnswers = validateAnswers(request.answers(), questions);
        int correctCount = (int) questions.stream()
                .filter(question -> question.correctAnswer().equalsIgnoreCase(
                        submittedAnswers.getOrDefault(question.questionId(), "").trim()))
                .count();
        int score = (int) Math.round(correctCount * 100.0 / questions.size());
        AssessmentResult result = analyzePerformance(questions, submittedAnswers, attempt.getSkill());

        attempt.setStudentAnswersJson(writeJson(request.answers(), "Assessment answers could not be saved."));
        attempt.setCorrectCount(correctCount);
        attempt.setScore(score);
        attempt.setResultJson(writeJson(result, "Assessment result could not be saved."));
        attempt.setSubmittedAt(Instant.now());
        AssessmentAttempt saved = attemptRepository.save(attempt);
        return toResponse(saved, questions, result);
    }

    private List<StoredQuestion> parseAndValidateQuestions(String generatedContent, String skill,
                                                           String difficulty, int expectedCount) {
        JsonNode questionsNode;
        try {
            String json = stripJsonFence(generatedContent);
            questionsNode = objectMapper.readTree(json).path("questions");
        } catch (JsonProcessingException | IllegalArgumentException exception) {
            logger.warn("Gemini assessment output was not valid JSON.");
            throw invalidGeneratedQuestions();
        }
        if (!questionsNode.isArray() || questionsNode.size() != expectedCount) {
            logger.warn("Gemini returned an unexpected assessment question count.");
            throw invalidGeneratedQuestions();
        }

        List<StoredQuestion> questions = new ArrayList<>(expectedCount);
        Set<String> questionFingerprints = new LinkedHashSet<>();
        Set<String> targetTokens = tokenize(skill);

        for (int index = 0; index < questionsNode.size(); index++) {
            JsonNode node = questionsNode.get(index);
            String question = textField(node, "question");
            String correctAnswer = textField(node, "correctAnswer");
            String topic = textField(node, "topic");
            String questionDifficulty = textField(node, "difficulty");
            JsonNode optionsNode = node.path("options");
            if (question == null || question.isBlank()
                    || question.length() > 2000
                    || topic == null || topic.isBlank() || topic.length() > 100
                    || !VALID_DIFFICULTIES.contains(questionDifficulty == null
                    ? "" : questionDifficulty.toLowerCase(Locale.ROOT))
                    || !difficulty.equalsIgnoreCase(questionDifficulty)
                    || !optionsNode.isArray() || optionsNode.size() != 4) {
                logger.warn("Gemini returned an invalid assessment question.");
                throw invalidGeneratedQuestions();
            }

            List<String> options = new ArrayList<>(4);
            Set<String> uniqueOptions = new LinkedHashSet<>();
            for (JsonNode optionNode : optionsNode) {
                if (!optionNode.isTextual() || optionNode.asText().isBlank()
                        || optionNode.asText().length() > 300) {
                    throw invalidGeneratedQuestions();
                }
                String option = optionNode.asText().trim();
                if (!uniqueOptions.add(normalize(option))) {
                    throw invalidGeneratedQuestions();
                }
                options.add(option);
            }
            if (correctAnswer == null || options.stream().noneMatch(
                    option -> option.equalsIgnoreCase(correctAnswer.trim()))) {
                throw invalidGeneratedQuestions();
            }
            String fingerprint = normalize(question);
            if (!questionFingerprints.add(fingerprint)) {
                logger.warn("Gemini returned duplicate assessment questions.");
                throw invalidGeneratedQuestions();
            }
            if (!isRelatedToRequestedSkill(skill, targetTokens, question, topic, options)) {
                logger.warn("Gemini returned an assessment question unrelated to the requested topic.");
                throw invalidGeneratedQuestions();
            }

            questions.add(new StoredQuestion(
                    (long) index + 1, question.trim(), List.copyOf(options),
                    options.stream().filter(option -> option.equalsIgnoreCase(correctAnswer.trim())).findFirst().orElseThrow(),
                    topic.trim(), questionDifficulty.trim()));
        }
        return List.copyOf(questions);
    }

    private Map<Long, String> validateAnswers(List<AssessmentAnswerRequest> answers,
                                               List<StoredQuestion> questions) {
        Map<Long, StoredQuestion> questionById = questions.stream()
                .collect(Collectors.toMap(StoredQuestion::questionId, question -> question));
        Map<Long, String> selectedByQuestion = new LinkedHashMap<>();
        for (AssessmentAnswerRequest answer : answers) {
            StoredQuestion question = questionById.get(answer.questionId());
            if (question == null) {
                throw new IllegalArgumentException("An answer refers to a question outside this assessment.");
            }
            String selected = answer.selectedAnswer().trim();
            if (question.options().stream().noneMatch(option -> option.equalsIgnoreCase(selected))) {
                throw new IllegalArgumentException("Choose one of the options shown for each answer.");
            }
            if (selectedByQuestion.putIfAbsent(answer.questionId(), selected) != null) {
                throw new IllegalArgumentException("Each question can only have one submitted answer.");
            }
        }
        return selectedByQuestion;
    }

    private AssessmentResult analyzePerformance(List<StoredQuestion> questions,
                                                Map<Long, String> selectedAnswers, String skill) {
        Map<String, TopicScore> byTopic = new LinkedHashMap<>();
        for (StoredQuestion question : questions) {
            TopicScore topicScore = byTopic.computeIfAbsent(question.topic(), ignored -> new TopicScore());
            topicScore.total++;
            if (question.correctAnswer().equalsIgnoreCase(
                    selectedAnswers.getOrDefault(question.questionId(), "").trim())) {
                topicScore.correct++;
            }
        }

        List<AssessmentTopicPerformance> topicPerformance = byTopic.entrySet().stream()
                .map(entry -> new AssessmentTopicPerformance(
                        entry.getKey(),
                        entry.getValue().correct,
                        entry.getValue().total,
                        (int) Math.round(entry.getValue().correct * 100.0 / entry.getValue().total)))
                .toList();
        List<String> strongAreas = topicPerformance.stream()
                .filter(topic -> topic.percentage() >= 80)
                .map(AssessmentTopicPerformance::topic)
                .toList();
        List<AssessmentTopicPerformance> weakTopics = topicPerformance.stream()
                .filter(topic -> topic.percentage() < 60)
                .toList();
        List<String> needsImprovement = weakTopics.stream()
                .map(AssessmentTopicPerformance::topic)
                .toList();
        List<String> skillGaps = weakTopics.stream()
                .map(topic -> skill + " - " + topic.topic() + " (" + topic.percentage() + "%)")
                .toList();
        List<String> recommendations = weakTopics.stream()
                .map(topic -> "Review " + topic.topic() + " in " + skill
                        + ", then practice problems that apply those concepts.")
                .toList();

        return new AssessmentResult(topicPerformance, strongAreas, needsImprovement, skillGaps, recommendations);
    }

    private AssessmentAttemptResponse toResponse(AssessmentAttempt attempt, List<StoredQuestion> storedQuestions,
                                                 AssessmentResult result) {
        List<AssessmentQuestionResponse> publicQuestions = storedQuestions.stream()
                .map(question -> new AssessmentQuestionResponse(
                        question.questionId(), question.question(), question.options(), question.topic(), question.difficulty()))
                .toList();
        boolean submitted = attempt.getSubmittedAt() != null;
        return new AssessmentAttemptResponse(
                attempt.getId(),
                attempt.getSkill(),
                attempt.getDifficulty(),
                attempt.getNumberOfQuestions(),
                publicQuestions,
                submitted,
                submitted ? attempt.getScore() : null,
                submitted ? attempt.getCorrectCount() : null,
                result == null ? List.of() : result.topicPerformance(),
                result == null ? List.of() : result.strongAreas(),
                result == null ? List.of() : result.needsImprovement(),
                result == null ? List.of() : result.skillGaps(),
                result == null ? List.of() : result.recommendations(),
                attempt.getCreatedAt(),
                attempt.getSubmittedAt());
    }

    private List<StoredQuestion> readQuestions(AssessmentAttempt attempt) {
        try {
            return objectMapper.readValue(attempt.getGeneratedQuestionsJson(), STORED_QUESTION_LIST);
        } catch (JsonProcessingException exception) {
            logger.error("Stored assessment questions could not be read for attempt {}.", attempt.getId());
            throw new IllegalStateException("Stored assessment data could not be read.");
        }
    }

    private AssessmentResult readResult(AssessmentAttempt attempt) {
        if (attempt.getResultJson() == null) {
            return null;
        }
        try {
            return objectMapper.readValue(attempt.getResultJson(), AssessmentResult.class);
        } catch (JsonProcessingException exception) {
            logger.error("Stored assessment result could not be read for attempt {}.", attempt.getId());
            throw new IllegalStateException("Stored assessment result could not be read.");
        }
    }

    private String writeJson(Object value, String message) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            logger.error("Assessment data could not be serialized.");
            throw new IllegalStateException(message);
        }
    }

    private String textField(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        return value != null && value.isTextual() ? value.asText().trim() : null;
    }

    private String stripJsonFence(String content) {
        if (content == null) {
            throw invalidGeneratedQuestions();
        }
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }
        return trimmed;
    }

    private boolean isRelatedToRequestedSkill(String skill, Set<String> targetTokens, String question,
                                              String topic, List<String> options) {
        Set<String> contentTokens = tokenize(question + " " + topic + " " + String.join(" ", options));
        if (targetTokens.stream().anyMatch(contentTokens::contains)) {
            return true;
        }
        return normalize(skill).length() >= 3 && normalize(question + " " + topic).contains(normalize(skill));
    }

    private Set<String> tokenize(String value) {
        return Arrays.stream(Normalizer.normalize(value, Normalizer.Form.NFKC)
                        .toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}+#]+"))
                .filter(token -> !token.isBlank() && !Set.of("a", "an", "and", "for", "in", "of", "on", "the", "to")
                        .contains(token))
                .collect(Collectors.toSet());
    }

    private String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private boolean containsLetterOrDigit(String value) {
        return value.codePoints().anyMatch(Character::isLetterOrDigit);
    }

    private String capitalize(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private GeminiIntegrationException invalidGeneratedQuestions() {
        return new GeminiIntegrationException(HttpStatus.BAD_GATEWAY,
                "Gemini could not generate a valid assessment for this topic. Please try again.");
    }

    private record StoredQuestion(Long questionId, String question, List<String> options,
                                  String correctAnswer, String topic, String difficulty) {
    }

    private record AssessmentResult(List<AssessmentTopicPerformance> topicPerformance,
                                    List<String> strongAreas, List<String> needsImprovement,
                                    List<String> skillGaps, List<String> recommendations) {
    }

    private static final class TopicScore {
        private int correct;
        private int total;
    }
}
