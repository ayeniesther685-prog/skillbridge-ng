package org.skillbridge.email;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates and hashes verification tokens.
 *
 * The raw token (returned by generate()) is what goes into the email link.
 * Only its SHA-256 hash is stored in the database. A leaked DB dump cannot
 * be used to verify any account.
 *
 * Token length: 32 random bytes → ~43 base64url characters. High entropy,
 * practically unguessable.
 */
@Component
public class VerificationTokenHasher {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    /** Generate a new opaque token. This is what goes in the URL. */
    public String generate() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Hash the raw token for database storage. */
    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is required by every JVM. If this throws, the JVM is broken.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
