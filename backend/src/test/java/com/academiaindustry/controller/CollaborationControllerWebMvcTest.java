package com.academiaindustry.controller;

import com.academiaindustry.service.CollaborationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CollaborationController.class)
@Import({CollaborationController.class, CollaborationControllerWebMvcTest.MethodSecurityTestConfig.class})
class CollaborationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CollaborationService collaborationService;

    @Test
    @WithMockUser(roles = "STUDENT")
    void studentCanReadParticipantCollaborations() throws Exception {
        mockMvc.perform(get("/api/collaborations"))
                .andExpect(status().isOk());

        verify(collaborationService).getAll(anyString(), anyBoolean());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void studentCannotCreateCollaboration() throws Exception {
        mockMvc.perform(post("/api/collaborations")
                .contentType("application/json")
            .content("""
                {
                  "industryId": 1,
                  "institutionId": 1,
                  "title": "Test collaboration",
                  "description": "Test collaboration description",
                  "status": "REQUESTED"
                }
                """))
            .andExpect(status().isForbidden());
    }

    @EnableMethodSecurity
    @Configuration
    static class MethodSecurityTestConfig {

        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .build();
        }
    }
}
