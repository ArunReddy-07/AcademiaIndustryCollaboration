package com.academiaindustry.service;

import com.academiaindustry.dto.AssessmentAnswerRequest;
import com.academiaindustry.dto.GenerateAssessmentRequest;
import com.academiaindustry.dto.SubmitAssessmentRequest;
import com.academiaindustry.entity.AssessmentAttempt;
import com.academiaindustry.entity.Student;
import com.academiaindustry.exception.GeminiIntegrationException;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.AssessmentAttemptRepository;
import com.academiaindustry.repository.StudentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssessmentAttemptServiceTest {

    private AssessmentAttemptRepository attemptRepository;
    private StudentRepository studentRepository;
    private GeminiIntegrationService geminiIntegrationService;
    private ObjectMapper objectMapper;
    private AssessmentAttemptService service;
    private Student student;

    @BeforeEach
    void setUp() {
        attemptRepository = mock(AssessmentAttemptRepository.class);
        studentRepository = mock(StudentRepository.class);
        geminiIntegrationService = mock(GeminiIntegrationService.class);
        objectMapper = new ObjectMapper().findAndRegisterModules();
        service = new AssessmentAttemptService(
                attemptRepository, studentRepository, geminiIntegrationService, objectMapper);
        student = mock(Student.class);
    }

    @Test
    void generationPersistsOneValidatedAttemptWithoutReturningAnswerKey() throws Exception {
        when(studentRepository.findByUserEmailIgnoreCase("student@example.com")).thenReturn(Optional.of(student));
        when(geminiIntegrationService.generateAssessmentQuestions("Java Collections", "Medium", 5))
                .thenReturn(validQuestionsJson(5));
        when(attemptRepository.save(any(AssessmentAttempt.class))).thenAnswer(invocation -> {
            AssessmentAttempt saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 44L);
            ReflectionTestUtils.setField(saved, "createdAt", Instant.now());
            return saved;
        });

        var response = service.generate("student@example.com", request("  Java Collections  ", "Medium", 5));

        assertEquals(44L, response.attemptId());
        assertEquals("Java Collections", response.skill());
        assertEquals(5, response.questions().size());
        assertFalse(response.submitted());
        String publicResponse = objectMapper.writeValueAsString(response);
        assertFalse(publicResponse.contains("correctAnswer"));
        verify(geminiIntegrationService).generateAssessmentQuestions("Java Collections", "Medium", 5);
        verify(attemptRepository).save(any(AssessmentAttempt.class));
    }

    @Test
    void rejectsUnsupportedQuestionCountBeforeCallingGemini() {
        assertThrows(IllegalArgumentException.class,
                () -> service.generate("student@example.com", request("Java", "Medium", 7)));

        verify(geminiIntegrationService, never()).generateAssessmentQuestions(any(), any(), anyInt());
        verify(attemptRepository, never()).save(any());
    }

    @Test
    void rejectsMalformedGeminiOutputWithoutSavingAttempt() {
        when(studentRepository.findByUserEmailIgnoreCase("student@example.com")).thenReturn(Optional.of(student));
        when(geminiIntegrationService.generateAssessmentQuestions("Java", "Medium", 5))
                .thenReturn("{not-json");

        GeminiIntegrationException exception = assertThrows(GeminiIntegrationException.class,
                () -> service.generate("student@example.com", request("Java", "Medium", 5)));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatus());
        verify(attemptRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateQuestionsAndDuplicateOptions() {
        when(studentRepository.findByUserEmailIgnoreCase("student@example.com")).thenReturn(Optional.of(student));
        when(geminiIntegrationService.generateAssessmentQuestions("Java", "Medium", 5))
                .thenReturn(duplicateQuestionJson());

        assertThrows(GeminiIntegrationException.class,
                () -> service.generate("student@example.com", request("Java", "Medium", 5)));
        verify(attemptRepository, never()).save(any());

        when(geminiIntegrationService.generateAssessmentQuestions("Java", "Medium", 5))
                .thenReturn(duplicateOptionsJson());
        assertThrows(GeminiIntegrationException.class,
                () -> service.generate("student@example.com", request("Java", "Medium", 5)));
        verify(attemptRepository, never()).save(any());
    }

    @Test
    void rejectsQuestionsOutsideRequestedTopic() {
        when(studentRepository.findByUserEmailIgnoreCase("student@example.com")).thenReturn(Optional.of(student));
        when(geminiIntegrationService.generateAssessmentQuestions("Binary Trees", "Medium", 5))
                .thenReturn(unrelatedQuestionsJson());

        assertThrows(GeminiIntegrationException.class,
                () -> service.generate("student@example.com", request("Binary Trees", "Medium", 5)));
        verify(attemptRepository, never()).save(any());
    }

    @Test
    void submissionScoresOnBackendAndReturnsTopicAnalysisWithoutAnswers() throws Exception {
        AssessmentAttempt attempt = attemptWithQuestions();
        when(attemptRepository.findByIdAndStudentEmailIgnoreCaseForUpdate(44L, "student@example.com"))
                .thenReturn(Optional.of(attempt));
        when(attemptRepository.save(attempt)).thenReturn(attempt);
        List<AssessmentAnswerRequest> answers = List.of(
                new AssessmentAnswerRequest(1L, "option-A"),
                new AssessmentAnswerRequest(2L, "option-A"),
                new AssessmentAnswerRequest(3L, "option-A"));

        var response = service.submit("student@example.com", 44L, new SubmitAssessmentRequest(answers));

        assertTrue(response.submitted());
        assertEquals(60, response.score());
        assertEquals(3, response.correctCount());
        assertEquals(List.of("Lists"), response.strongAreas());
        assertEquals(List.of("Maps"), response.needsImprovement());
        assertEquals("Java Collections - Maps (0%)", response.skillGaps().getFirst());
        JsonNode json = objectMapper.valueToTree(response);
        assertFalse(json.toString().contains("correctAnswer"));
        assertFalse(json.get("questions").get(0).has("correctAnswer"));
        assertTrue(attempt.getGeneratedQuestionsJson().contains("correctAnswer"));
        assertEquals(60, attempt.getScore());
        assertTrue(attempt.getStudentAnswersJson().contains("option-A"));
        assertTrue(attempt.getResultJson().contains("recommendations"));
    }

    @Test
    void submissionRejectsAnswerForUnknownQuestion() {
        AssessmentAttempt attempt = attemptWithQuestions();
        when(attemptRepository.findByIdAndStudentEmailIgnoreCaseForUpdate(44L, "student@example.com"))
                .thenReturn(Optional.of(attempt));

        assertThrows(IllegalArgumentException.class,
                () -> service.submit("student@example.com", 44L,
                        new SubmitAssessmentRequest(List.of(new AssessmentAnswerRequest(99L, "option-A")))));
        verify(attemptRepository, never()).save(attempt);
    }

    @Test
    void studentCannotReadAnotherStudentsAttempt() {
        when(attemptRepository.findByIdAndStudentEmailIgnoreCase(44L, "other@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getAttempt("other@example.com", 44L));
    }

    @Test
    void submittedAttemptCannotBeRescoredWithDifferentAnswers() throws Exception {
        AssessmentAttempt attempt = attemptWithQuestions();
        attempt.setScore(60);
        attempt.setCorrectCount(3);
        attempt.setStudentAnswersJson("[{\"questionId\":1,\"selectedAnswer\":\"option-A\"}]");
        attempt.setResultJson("""
                {"topicPerformance":[],"strongAreas":[],"needsImprovement":[],"skillGaps":[],"recommendations":[]}
                """);
        attempt.setSubmittedAt(Instant.now());
        when(attemptRepository.findByIdAndStudentEmailIgnoreCaseForUpdate(44L, "student@example.com"))
                .thenReturn(Optional.of(attempt));

        var response = service.submit("student@example.com", 44L,
                new SubmitAssessmentRequest(List.of(new AssessmentAnswerRequest(1L, "option-B"))));

        assertEquals(60, response.score());
        assertEquals("option-A", objectMapper.readTree(attempt.getStudentAnswersJson()).get(0).get("selectedAnswer").asText());
        verify(attemptRepository, never()).save(attempt);
    }

    private AssessmentAttempt attemptWithQuestions() {
        AssessmentAttempt attempt = new AssessmentAttempt(
                student, "Java Collections", "Medium", 5, storedQuestionsJson());
        ReflectionTestUtils.setField(attempt, "id", 44L);
        ReflectionTestUtils.setField(attempt, "createdAt", Instant.now());
        return attempt;
    }

    private String storedQuestionsJson() {
        return """
                [
                  {"questionId":1,"question":"Which Java Collections type stores an ordered list?","options":["option-A","option-B","option-C","option-D"],"correctAnswer":"option-A","topic":"Lists","difficulty":"Medium"},
                  {"questionId":2,"question":"Which Java Collections type stores an ordered list? second","options":["option-A","option-B","option-C","option-D"],"correctAnswer":"option-A","topic":"Lists","difficulty":"Medium"},
                  {"questionId":3,"question":"Which Java Collections type stores an ordered list? third","options":["option-A","option-B","option-C","option-D"],"correctAnswer":"option-A","topic":"Lists","difficulty":"Medium"},
                  {"questionId":4,"question":"Which Java Collections type maps keys to values?","options":["option-A","option-B","option-C","option-D"],"correctAnswer":"option-B","topic":"Maps","difficulty":"Medium"},
                  {"questionId":5,"question":"Which Java Collections type maps keys to values? second","options":["option-A","option-B","option-C","option-D"],"correctAnswer":"option-B","topic":"Maps","difficulty":"Medium"}
                ]
                """;
    }

    private String validQuestionsJson(int count) {
        String questions = java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> """
                        {"question":"In Java Collections, which concept applies to question %d?",
                         "options":["option-A","option-B","option-C","option-D"],
                         "correctAnswer":"option-A","topic":"Collections","difficulty":"Medium"}
                        """.formatted(index + 1))
                .collect(java.util.stream.Collectors.joining(","));
        return "{\"questions\":[" + questions + "]}";
    }

    private String duplicateQuestionJson() {
        String question = """
                {"question":"In Java, which collection stores unique elements?",
                 "options":["Set","List","Queue","Map"],"correctAnswer":"Set",
                 "topic":"Collections","difficulty":"Medium"}
                """;
        return "{\"questions\":[" + String.join(",", java.util.Collections.nCopies(5, question)) + "]}";
    }

    private String duplicateOptionsJson() {
        String question = """
                {"question":"In Java, which collection stores unique elements?",
                 "options":["Set","Set","Queue","Map"],"correctAnswer":"Set",
                 "topic":"Collections","difficulty":"Medium"}
                """;
        String distinctQuestion = question.replace("stores unique elements", "handles unique elements");
        return "{\"questions\":[" + String.join(",", java.util.Collections.nCopies(5, distinctQuestion)) + "]}";
    }

    private String unrelatedQuestionsJson() {
        String question = """
                {"question":"What is the chemical symbol for gold?",
                 "options":["Au","Ag","Fe","O"],"correctAnswer":"Au",
                 "topic":"Chemistry","difficulty":"Medium"}
                """;
        return "{\"questions\":[" + String.join(",", java.util.Collections.nCopies(5, question)) + "]}";
    }

    private GenerateAssessmentRequest request(String skill, String difficulty, int count) {
        GenerateAssessmentRequest request = new GenerateAssessmentRequest();
        request.setSkill(skill);
        request.setDifficulty(difficulty);
        request.setNumberOfQuestions(count);
        return request;
    }
}
