package com.academiaindustry.controller;

import com.academiaindustry.config.SecurityConfig;
import com.academiaindustry.security.JwtAuthenticationFilter;
import com.academiaindustry.security.JwtService;
import com.academiaindustry.service.GeminiIntegrationService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiTestController.class)
@Import({SecurityConfig.class, AiTestControllerWebMvcTest.JwtFilterConfiguration.class})
@TestPropertySource(properties = "app.security.allowed-origins=http://localhost:5173")
class AiTestControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GeminiIntegrationService geminiIntegrationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void endpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/ai/test"))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpointIsRestrictedToAdministrators() throws Exception {
        mockMvc.perform(get("/api/ai/test").with(user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorReceivesGeneratedQuestion() throws Exception {
        when(geminiIntegrationService.generateJavaCollectionsQuestion())
                .thenReturn("Question about Java Collections");

        mockMvc.perform(get("/api/ai/test").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Question about Java Collections"));

        verify(geminiIntegrationService).generateJavaCollectionsQuestion();
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
