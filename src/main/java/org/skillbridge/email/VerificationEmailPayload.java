package org.skillbridge.email;

import java.time.Instant;

/**
 * Data passed to the template renderer. Deliberately minimal — no password,
 * no phone, nothing the recipient doesn't already know about themselves.
 */
public record VerificationEmailPayload(
        String recipientFirstName,
        String recipientEmail,
        String verifyUrl,
        Instant expiresAt,
        int expiresInHours
) {}
