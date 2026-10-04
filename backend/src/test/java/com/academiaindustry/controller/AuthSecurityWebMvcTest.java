package com.academiaindustry.controller;

import com.academiaindustry.config.SecurityConfig;
import com.academiaindustry.dto.AuthResponse;
import com.academiaindustry.security.JwtAuthenticationFilter;
import com.academiaindustry.security.JwtService;
import com.academiaindustry.service.AuthService;
import com.academiaindustry.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, AuthSecurityWebMvcTest.JwtFilterConfiguration.class})
@TestPropertySource(properties = {
        "app.security.allowed-origins=http://localhost:5173"
})
class AuthSecurityWebMvcTest {

    private static final String FRONTEND_ORIGIN = "https://academiaindustryfrontend.onrender.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void loginEndpointIsPublic() throws Exception {
        when(authService.login(any())).thenReturn(new AuthResponse());

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"student@example.com","password":"password123"}
                                """))
                .andExpect(status().isOk());
        verify(authService).login(any());
    }

    @Test
    void registrationEndpointIsPublic() throws Exception {
        when(authService.register(any())).thenReturn(new AuthResponse());

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"name":"Test Student","email":"student@example.com",
                                 "password":"password123","role":"STUDENT"}
                                """))
                .andExpect(status().isCreated());
        verify(authService).register(any());
    }

    @Test
    void passwordResetRequestEndpointIsPublic() throws Exception {
        mockMvc.perform(post("/api/auth/password-reset/request")
                        .contentType("application/json")
                        .content("""
                                {"email":"student@example.com"}
                                """))
                .andExpect(status().isAccepted());
        verify(passwordResetService).requestCode("student@example.com");
    }

    @Test
    void passwordResetCompletionEndpointIsPublic() throws Exception {
        mockMvc.perform(post("/api/auth/password-reset/complete")
                        .contentType("application/json")
                        .content("""
                                {"email":"student@example.com","code":"123456",
                                 "newPassword":"new-password-123"}
                                """))
                .andExpect(status().isOk());
        verify(passwordResetService).completeReset(any());
    }

    @Test
    void corsPreflightAllowsConfiguredFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, FRONTEND_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                        "GET,POST,PUT,PATCH,DELETE,OPTIONS"));
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
