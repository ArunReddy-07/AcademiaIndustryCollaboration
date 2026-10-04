package com.academiaindustry.exception;

public class InvalidPasswordResetException extends RuntimeException {

    public InvalidPasswordResetException() {
        super("The verification code is invalid or expired. Request a new code and try again.");
    }
}
