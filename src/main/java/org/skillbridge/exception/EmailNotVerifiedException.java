package org.skillbridge.exception;

/**
 * Thrown at login or on protected endpoints when the user's email is not verified.
 * Mapped to HTTP 403 with a specific error code so the frontend can offer "resend
 * verification" without forcing a logout.
 */
public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
