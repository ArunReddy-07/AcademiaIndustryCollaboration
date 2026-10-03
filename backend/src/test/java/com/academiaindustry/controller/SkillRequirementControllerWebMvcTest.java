package com.academiaindustry.controller;

import com.academiaindustry.security.OwnershipSecurity;
import com.academiaindustry.service.SkillRequirementService;
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

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SkillRequirementController.class)
@Import({SkillRequirementController.class, SkillRequirementControllerWebMvcTest.MethodSecurityTestConfig.class})
class SkillRequirementControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SkillRequirementService requirementService;

    @MockBean(name = "ownershipSecurity")
    private OwnershipSecurity ownershipSecurity;

    @Test
    @WithMockUser(username = "industry@example.com", roles = "INDUSTRY")
    void industryCannotAddRequirementsToAnotherCompanyOpportunity() throws Exception {
        when(ownershipSecurity.isOpportunityOwner(42L, "industry@example.com")).thenReturn(false);

        mockMvc.perform(post("/api/opportunities/42/skills")
                        .contentType("application/json")
                        .content("{\"skillId\":1,\"requiredLevel\":\"INTERMEDIATE\"}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(requirementService);
    }

    @Test
    @WithMockUser(username = "industry@example.com", roles = "INDUSTRY")
    void industryCanAddRequirementsToItsOwnOpportunity() throws Exception {
        when(ownershipSecurity.isOpportunityOwner(42L, "industry@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/opportunities/42/skills")
                        .contentType("application/json")
                        .content("{\"skillId\":1,\"requiredLevel\":\"INTERMEDIATE\"}"))
                .andExpect(status().isCreated());
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