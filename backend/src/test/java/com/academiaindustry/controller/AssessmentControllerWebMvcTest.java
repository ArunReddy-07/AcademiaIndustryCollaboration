package com.academiaindustry.controller;

import com.academiaindustry.config.SecurityConfig;
import com.academiaindustry.dto.AssessmentAttemptResponse;
import com.academiaindustry.security.JwtAuthenticationFilter;
import com.academiaindustry.security.JwtService;
import com.academiaindustry.service.AssessmentAttemptService;
import com.academiaindustry.service.SkillAssessmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssessmentController.class)
@Import({SecurityConfig.class, AssessmentControllerWebMvcTest.JwtFilterConfiguration.class})
@TestPropertySource(properties = "app.security.allowed-origins=http://localhost:5173")
class AssessmentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssessmentAttemptService assessmentAttemptService;

    @MockitoBean
    private SkillAssessmentService skillAssessmentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void assessmentGenerationRequiresStudentRole() throws Exception {
        mockMvc.perform(post("/api/assessments/generate")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .contentType("application/json")
                        .content("""
                                {"skill":"Java Collections","difficulty":"Medium","numberOfQuestions":5}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentCanGenerateAssessmentUsingTheirAuthenticatedEmail() throws Exception {
        when(assessmentAttemptService.generate(eq("student@example.com"), any()))
                .thenReturn(new AssessmentAttemptResponse(
                        42L, "Java Collections", "Medium", 5, List.of(), false,
                        null, null, List.of(), List.of(), List.of(), List.of(), List.of(),
                        null, null));

        mockMvc.perform(post("/api/assessments/generate")
                        .with(user("student@example.com").roles("STUDENT"))
                        .contentType("application/json")
                        .content("""
                                {"skill":"Java Collections","difficulty":"Medium","numberOfQuestions":5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptId").value(42))
                .andExpect(jsonPath("$.submitted").value(false));

        verify(assessmentAttemptService).generate(eq("student@example.com"), any());
    }

    @TestConfiguration
    static class JwtFilterConfiguration {

        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService,
                                                         UserDetailsService userDetailsService) {
            return new JwtAuthenticationFilter(jwtService, userDetailsService);
        }
    }
}
