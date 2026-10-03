package com.academiaindustry.service;

import com.academiaindustry.dto.AuthResponse;
import com.academiaindustry.dto.RegisterRequest;
import com.academiaindustry.dto.UserResponse;
import com.academiaindustry.entity.Role;
import com.academiaindustry.entity.User;
import com.academiaindustry.exception.DuplicateResourceException;
import com.academiaindustry.repository.UserRepository;
import com.academiaindustry.security.JwtService;
import com.academiaindustry.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

    private UserRepository userRepository;
    private AuthServiceImpl authService;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        jwtService = mock(JwtService.class);
        authService = new AuthServiceImpl(
                userRepository,
                mock(UserService.class),
                mock(AuthenticationManager.class),
                jwtService);
    }

    @Test
    void publicRegistrationRejectsPrivilegedRoles() {
        RegisterRequest request = requestWithRole(Role.ADMIN);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }

    @Test
    void publicRegistrationRejectsFacultyRole() {
        RegisterRequest request = requestWithRole(Role.FACULTY);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }

    @Test
    void registrationRejectsDuplicateEmail() {
        RegisterRequest request = requestWithRole(Role.STUDENT);
        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
    }

    @Test
    void registrationReportsEmailConflictWhenDatabaseRejectsDuplicate() {
        RegisterRequest request = requestWithRole(Role.STUDENT);
        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(false);
        UserService userService = mock(UserService.class);
        when(userService.create(any())).thenThrow(new DataIntegrityViolationException("duplicate email"));
        authService = new AuthServiceImpl(userRepository, userService,
                mock(AuthenticationManager.class), jwtService);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class, () -> authService.register(request));

        assertEquals("A user with this email already exists.", exception.getMessage());
    }

    @Test
    void registrationReturnsTokenAndUserWithoutPassword() {
        RegisterRequest request = requestWithRole(Role.STUDENT);
        User user = new User(request.getName(), request.getEmail(), "hashed-password", request.getRole());
        UserResponse response = new UserResponse();
        response.setEmail(request.getEmail());
        response.setRole(Role.STUDENT);

        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(false);
        when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("token");
        when(jwtService.getExpirationMillis()).thenReturn(3600000L);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserService userService = mock(UserService.class);
        when(userService.create(any())).thenReturn(response);
        authService = new AuthServiceImpl(userRepository, userService,
                mock(AuthenticationManager.class), jwtService);

        AuthResponse result = authService.register(request);

        assertEquals("token", result.getAccessToken());
        assertEquals(request.getEmail(), result.getUser().getEmail());
    }

    @Test
    void publicRegistrationAcceptsInstitutionRoleThroughExistingAuthService() {
        RegisterRequest request = requestWithRole(Role.INSTITUTION);
        User user = new User(request.getName(), request.getEmail(), "hashed-password", request.getRole());
        UserResponse response = new UserResponse();
        response.setEmail(request.getEmail());
        response.setRole(Role.INSTITUTION);
        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(false);
        when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("token");
        when(jwtService.getExpirationMillis()).thenReturn(3600000L);
        UserService userService = mock(UserService.class);
        when(userService.create(any())).thenReturn(response);
        authService = new AuthServiceImpl(userRepository, userService,
                mock(AuthenticationManager.class), jwtService);

        AuthResponse result = authService.register(request);

        assertEquals(Role.INSTITUTION, result.getUser().getRole());
        assertEquals("token", result.getAccessToken());
    }

    private RegisterRequest requestWithRole(Role role) {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRole(role);
        return request;
    }
}
