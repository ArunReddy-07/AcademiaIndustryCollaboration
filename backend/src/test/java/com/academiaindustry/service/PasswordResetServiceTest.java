package com.academiaindustry.service;

import com.academiaindustry.dto.PasswordResetCompleteRequest;
import com.academiaindustry.entity.PasswordResetToken;
import com.academiaindustry.entity.Role;
import com.academiaindustry.entity.User;
import com.academiaindustry.exception.InvalidPasswordResetException;
import com.academiaindustry.repository.PasswordResetTokenRepository;
import com.academiaindustry.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordResetServiceTest {

    private UserRepository userRepository;
    private PasswordResetTokenRepository tokenRepository;
    private PasswordResetEmailService emailService;
    private PasswordEncoder passwordEncoder;
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        tokenRepository = mock(PasswordResetTokenRepository.class);
        emailService = mock(PasswordResetEmailService.class);
        passwordEncoder = new BCryptPasswordEncoder();
        passwordResetService = new PasswordResetService(
                userRepository, tokenRepository, passwordEncoder, emailService);
    }

    @Test
    void requestForUnknownEmailDoesNotRevealOrSendAnything() {
        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        passwordResetService.requestCode("missing@example.com");

        verify(emailService, never()).sendCode(any(), any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void requestSavesOnlyHashedCodeAndSendsItToRegisteredEmail() {
        User user = user();
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.findById(userId(user))).thenReturn(Optional.empty());

        passwordResetService.requestCode(user.getEmail());

        var tokenCaptor = org.mockito.ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken token = tokenCaptor.getValue();
        var codeCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailService).sendCode(org.mockito.ArgumentMatchers.eq(user.getEmail()), codeCaptor.capture());
        assertFalse(token.getCodeHash().equals(codeCaptor.getValue()));
        assertTrue(passwordEncoder.matches(codeCaptor.getValue(), token.getCodeHash()));
        assertTrue(token.getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    void validCodeChangesPasswordAndRemovesResetToken() {
        User user = user();
        String code = "248613";
        PasswordResetToken token = new PasswordResetToken(
                userId(user), passwordEncoder.encode(code), Instant.now(), Instant.now().plusSeconds(600));
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.findById(userId(user))).thenReturn(Optional.of(token));

        PasswordResetCompleteRequest request = completeRequest(user.getEmail(), code, "new-password-123");
        passwordResetService.completeReset(request);

        assertTrue(passwordEncoder.matches("new-password-123", user.getPassword()));
        verify(userRepository).save(user);
        verify(tokenRepository).delete(token);
    }

    @Test
    void invalidCodeIncrementsFailedAttempts() {
        User user = user();
        PasswordResetToken token = new PasswordResetToken(
                userId(user), passwordEncoder.encode("248613"), Instant.now(), Instant.now().plusSeconds(600));
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.findById(userId(user))).thenReturn(Optional.of(token));

        assertThrows(InvalidPasswordResetException.class,
                () -> passwordResetService.completeReset(
                        completeRequest(user.getEmail(), "000000", "new-password-123")));

        assertEquals(1, token.getFailedAttempts());
        verify(tokenRepository).save(token);
        verify(userRepository, never()).save(any());
    }

    @Test
    void fifthInvalidCodeInvalidatesTheResetCode() {
        User user = user();
        PasswordResetToken token = new PasswordResetToken(
                userId(user), passwordEncoder.encode("248613"), Instant.now(), Instant.now().plusSeconds(600));
        for (int attempt = 0; attempt < 4; attempt++) {
            token.incrementFailedAttempts();
        }
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.findById(userId(user))).thenReturn(Optional.of(token));

        assertThrows(InvalidPasswordResetException.class,
                () -> passwordResetService.completeReset(
                        completeRequest(user.getEmail(), "000000", "new-password-123")));

        verify(tokenRepository).delete(token);
        verify(tokenRepository, never()).save(token);
        verify(userRepository, never()).save(any());
    }

    private User user() {
        User user = new User("Test User", "test@example.com", passwordEncoder.encode("old-password-123"), Role.STUDENT);
        ReflectionTestUtils.setField(user, "id", 42L);
        return user;
    }

    private Long userId(User user) {
        return (Long) ReflectionTestUtils.getField(user, "id");
    }

    private PasswordResetCompleteRequest completeRequest(String email, String code, String newPassword) {
        PasswordResetCompleteRequest request = new PasswordResetCompleteRequest();
        request.setEmail(email);
        request.setCode(code);
        request.setNewPassword(newPassword);
        return request;
    }
}
