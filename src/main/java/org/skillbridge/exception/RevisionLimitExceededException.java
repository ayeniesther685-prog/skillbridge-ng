package org.skillbridge.exception;

public class RevisionLimitExceededException extends RuntimeException {
    public RevisionLimitExceededException(String message) { super(message); }
}
