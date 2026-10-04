package com.academiaindustry.service;

import com.academiaindustry.dto.PasswordResetCompleteRequest;
import com.academiaindustry.entity.PasswordResetToken;
import com.academiaindustry.entity.User;
import com.academiaindustry.exception.InvalidPasswordResetException;
import com.academiaindustry.repository.PasswordResetTokenRepository;
import com.academiaindustry.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final Duration CODE_LIFETIME = Duration.ofMinutes(10);
    private static final Duration RESEND_INTERVAL = Duration.ofSeconds(60);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetEmailService emailService;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                PasswordResetEmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void requestCode(String email) {
        Optional<User> account = userRepository.findByEmailIgnoreCase(email);
        if (account.isEmpty()) {
            return;
        }

        User user = account.get();
        Instant now = Instant.now();
        Optional<PasswordResetToken> existingToken = tokenRepository.findById(user.getId());
        if (existingToken.isPresent()
                && existingToken.get().getCreatedAt().isAfter(now.minus(RESEND_INTERVAL))) {
            return;
        }

        String code = String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
        PasswordResetToken token = existingToken.orElseGet(
                () -> new PasswordResetToken(user.getId(), "", now, now.plus(CODE_LIFETIME)));
        token.setCodeHash(passwordEncoder.encode(code));
        token.setCreatedAt(now);
        token.setExpiresAt(now.plus(CODE_LIFETIME));
        tokenRepository.save(token);
        emailService.sendCode(user.getEmail(), code);
    }

    @Transactional(noRollbackFor = InvalidPasswordResetException.class)
    public void completeReset(PasswordResetCompleteRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(InvalidPasswordResetException::new);
        PasswordResetToken token = tokenRepository.findById(user.getId())
                .orElseThrow(InvalidPasswordResetException::new);

        Instant now = Instant.now();
        if (!now.isBefore(token.getExpiresAt()) || token.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            tokenRepository.delete(token);
            throw new InvalidPasswordResetException();
        }

        if (!passwordEncoder.matches(request.getCode(), token.getCodeHash())) {
            token.incrementFailedAttempts();
            if (token.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                tokenRepository.delete(token);
            } else {
                tokenRepository.save(token);
            }
            throw new InvalidPasswordResetException();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        tokenRepository.delete(token);
    }
}
