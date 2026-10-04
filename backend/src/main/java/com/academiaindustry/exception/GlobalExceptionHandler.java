package com.academiaindustry.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(InvalidPasswordResetException.class)
        public ResponseEntity<ApiErrorResponse> handleInvalidPasswordReset(
                        InvalidPasswordResetException exception, HttpServletRequest request) {
                return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
        }

        @ExceptionHandler(PasswordResetEmailException.class)
        public ResponseEntity<ApiErrorResponse> handlePasswordResetEmail(
                        PasswordResetEmailException exception, HttpServletRequest request) {
                logger.error("Password reset email delivery failed for request {}", request.getRequestURI(), exception);
                return buildResponse(HttpStatus.SERVICE_UNAVAILABLE,
                                "Password reset email could not be sent. Please try again later.",
                                request.getRequestURI());
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiErrorResponse> handleAccessDenied(
                        AccessDeniedException exception, HttpServletRequest request) {
                return buildResponse(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.",
                                request.getRequestURI());
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiErrorResponse> handleBadCredentials(
                        BadCredentialsException exception, HttpServletRequest request) {
                return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password.", request.getRequestURI());
        }

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ApiErrorResponse> handleDuplicateResource(
                        DuplicateResourceException exception, HttpServletRequest request) {
                return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI());
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
                        DataIntegrityViolationException exception, HttpServletRequest request) {
                return buildResponse(HttpStatus.CONFLICT,
                                "This item conflicts with existing data. Check for a duplicate and try again.",
                                request.getRequestURI());
        }

        @ExceptionHandler(BusinessRuleException.class)
        public ResponseEntity<ApiErrorResponse> handleBusinessRule(
                        BusinessRuleException exception, HttpServletRequest request) {
                return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI());
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
                        IllegalArgumentException exception, HttpServletRequest request) {
                return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
        }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiErrorResponse> handleNoSuchElement(
            NoSuchElementException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> validationErrors.put(error.getField(), error.getDefaultMessage()));

        ApiErrorResponse response = createResponse(
                HttpStatus.BAD_REQUEST, "Request validation failed.", request.getRequestURI());
        response.setValidationErrors(validationErrors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        exception.getConstraintViolations()
                .forEach(violation -> validationErrors.put(
                        violation.getPropertyPath().toString(), violation.getMessage()));

        ApiErrorResponse response = createResponse(
                HttpStatus.BAD_REQUEST, "Request validation failed.", request.getRequestURI());
        response.setValidationErrors(validationErrors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Request body is invalid or malformed.", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception, HttpServletRequest request) {
        logger.error("Unexpected error handling request {}", request.getRequestURI(), exception);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred.", request.getRequestURI());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status, String message, String path) {
        return ResponseEntity.status(status).body(createResponse(status, message, path));
    }

    private ApiErrorResponse createResponse(HttpStatus status, String message, String path) {
        return new ApiErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path);
    }
}