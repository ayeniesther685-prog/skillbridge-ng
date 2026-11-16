package org.skillbridge.service;

/**
 * Core contract for the email-verification flow. All operations are
 * idempotent at the API level: re-issuing a token invalidates old ones,
 * re-verifying a consumed token is a no-op, and re-requesting verification
 * for an already-verified user is a no-op.
 */
public interface EmailVerificationService {

    /**
     * Issue a fresh verification token for the given user and send the email.
     * Invalidates any outstanding email-channel tokens for that user.
     * Does nothing if the user is already verified.
     *
     * Called on new registration and on explicit resend request.
     *
     * @return true if an email was sent, false if the user was already verified
     */
    boolean issueVerification(Long userId);

    /**
     * Verify the given raw token. Marks the token consumed and the user verified.
     *
     * @throws org.skillbridge.exception.ResourceNotFoundException if token not found
     * @throws IllegalStateException if token is consumed or expired
     */
    void verify(String rawToken);

    /**
     * Resend verification for the given email address (case-insensitive).
     * Does nothing if the email doesn't exist (to prevent enumeration).
     * Does nothing if the user is already verified.
     */
    void resendForEmail(String email);

    /**
     * Background cleanup — purges expired token rows. Safe to call anytime.
     */
    int purgeExpired();
}
