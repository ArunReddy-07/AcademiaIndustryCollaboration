package com.academiaindustry.controller;

import com.academiaindustry.dto.AuthResponse;
import com.academiaindustry.dto.LoginRequest;
import com.academiaindustry.dto.PasswordResetCompleteRequest;
import com.academiaindustry.dto.PasswordResetRequest;
import com.academiaindustry.dto.RegisterRequest;
import com.academiaindustry.service.AuthService;
import com.academiaindustry.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<MessageResponse> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestCode(request.getEmail());
        return ResponseEntity.accepted().body(new MessageResponse(
                "If an account exists for that email, a verification code has been sent."));
    }

    @PostMapping("/password-reset/complete")
    public ResponseEntity<MessageResponse> completePasswordReset(
            @Valid @RequestBody PasswordResetCompleteRequest request) {
        passwordResetService.completeReset(request);
        return ResponseEntity.ok(new MessageResponse("Your password has been reset. You can now sign in."));
    }

    public record MessageResponse(String message) {
    }
}