package com.academiaindustry.controller;

import com.academiaindustry.entity.OpportunityType;
import com.academiaindustry.security.OwnershipSecurity;
import com.academiaindustry.service.CareerListingSkillRequirementService;
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

@WebMvcTest(CareerListingSkillRequirementController.class)
@Import({CareerListingSkillRequirementController.class,
        CareerListingSkillRequirementControllerWebMvcTest.MethodSecurityTestConfig.class})
class CareerListingSkillRequirementControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CareerListingSkillRequirementService requirementService;

    @MockBean(name = "ownershipSecurity")
    private OwnershipSecurity ownershipSecurity;

    @Test
    @WithMockUser(username = "company@example.com", roles = "INDUSTRY")
    void companyCannotAddSkillRequirementsToAnotherCompaniesInternship() throws Exception {
        when(ownershipSecurity.isCareerListingOwner(OpportunityType.INTERNSHIP, 14L, "company@example.com"))
                .thenReturn(false);

        mockMvc.perform(post("/api/career-listings/INTERNSHIP/14/skills")
                        .contentType("application/json")
                        .content("{\"skillId\":3,\"requiredLevel\":\"INTERMEDIATE\"}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(requirementService);
    }

    @Test
    @WithMockUser(username = "company@example.com", roles = "INDUSTRY")
    void ownerCanAddSkillRequirementsToItsInternship() throws Exception {
        when(ownershipSecurity.isCareerListingOwner(OpportunityType.INTERNSHIP, 14L, "company@example.com"))
                .thenReturn(true);

        mockMvc.perform(post("/api/career-listings/INTERNSHIP/14/skills")
                        .contentType("application/json")
                        .content("{\"skillId\":3,\"requiredLevel\":\"INTERMEDIATE\"}"))
                .andExpect(status().isCreated());
    }

    @EnableMethodSecurity
    @Configuration
    static class MethodSecurityTestConfig {

        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .build();
        }
    }
}
