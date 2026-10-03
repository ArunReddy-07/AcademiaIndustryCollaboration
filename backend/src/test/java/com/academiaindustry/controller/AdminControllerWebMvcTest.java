package com.academiaindustry.controller;

import com.academiaindustry.service.ApplicationService;
import com.academiaindustry.service.CollaborationService;
import com.academiaindustry.service.InstitutionService;
import com.academiaindustry.service.OpportunityService;
import com.academiaindustry.service.PlacementService;
import com.academiaindustry.service.SkillService;
import com.academiaindustry.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import({AdminController.class, AdminControllerWebMvcTest.MethodSecurityTestConfig.class})
class AdminControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private SkillService skillService;
    @MockBean
    private InstitutionService institutionService;
    @MockBean
    private OpportunityService opportunityService;
    @MockBean
    private ApplicationService applicationService;
    @MockBean
    private CollaborationService collaborationService;
    @MockBean
    private PlacementService placementService;
    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessManagementEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void nonAdminCannotAccessManagementEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @EnableMethodSecurity
    @Configuration
    static class MethodSecurityTestConfig {

        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated())
                .build();
        }
    }
}
