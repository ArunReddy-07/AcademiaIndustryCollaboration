package com.academiaindustry.service;

import com.academiaindustry.exception.PasswordResetEmailException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetEmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String from;
    private final String smtpHost;

    public PasswordResetEmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                                     @Value("${app.mail.from:no-reply@example.com}") String from,
                                     @Value("${spring.mail.host:}") String smtpHost) {
        this.mailSenderProvider = mailSenderProvider;
        this.from = from;
        this.smtpHost = smtpHost;
    }

    public void sendCode(String recipient, String code) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || smtpHost.isBlank()) {
            throw new PasswordResetEmailException("Password reset email is not configured. Set the SMTP environment variables.");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient);
        message.setSubject("Your Academia × Industry Portal password reset code");
        message.setText("Your password reset code is " + code
                + ". It expires in 10 minutes. If you did not request this code, you can ignore this email.");
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            throw new PasswordResetEmailException("Unable to send the password reset email.", exception);
        }
    }
}
