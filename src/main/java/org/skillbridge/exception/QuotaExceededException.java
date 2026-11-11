package org.skillbridge.exception;

import java.time.Instant;

public class QuotaExceededException extends RuntimeException {

    private final String resetAtDescription;
    private final Instant resetAt;

    public QuotaExceededException(String message, String resetAtDescription, Instant resetAt) {
        super(message);
        this.resetAtDescription = resetAtDescription;
        this.resetAt = resetAt;
    }

    public String getResetAtDescription() { return resetAtDescription; }
    public Instant getResetAt() { return resetAt; }
}