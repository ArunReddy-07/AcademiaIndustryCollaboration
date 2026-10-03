package com.academiaindustry.controller;

import com.academiaindustry.security.OwnershipSecurity;
import com.academiaindustry.service.StudentService;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
@Import({StudentController.class, InstitutionStudentControllerWebMvcTest.MethodSecurityTestConfig.class})
class InstitutionStudentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @MockBean(name = "ownershipSecurity")
    private OwnershipSecurity ownershipSecurity;

    @Test
    @WithMockUser(username = "office@example.edu", roles = "INSTITUTION")
    void institutionStudentSearchUsesTheAccountScopedService() throws Exception {
        mockMvc.perform(get("/api/students").param("skillId", "6"))
                .andExpect(status().isOk());

        verify(studentService).getForInstitution("office@example.edu", 6L);
        verify(studentService, org.mockito.Mockito.never()).getAll();
    }

    @Test
    @WithMockUser(username = "office@example.edu", roles = "INSTITUTION")
    void institutionCannotReadAnotherInstitutionsStudentProfile() throws Exception {
        when(ownershipSecurity.isStudentInstitutionOwner(22L, "office@example.edu")).thenReturn(false);

        mockMvc.perform(get("/api/students/22"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(studentService);
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
