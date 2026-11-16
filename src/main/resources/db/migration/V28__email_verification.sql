-- =============================================================================
-- V28 — Email verification (Phase 3A)
-- =============================================================================
-- Adds email-verification tracking to users and a token table for one-time,
-- expiring, hashed verification links.
--
-- BACKFILL POLICY (Option A):
--   All users created before this migration are marked email_verified = TRUE.
--   This preserves their access to the platform. New registrations created
--   after this migration default to email_verified = FALSE and must complete
--   the verification flow before accessing protected functionality.
--
-- SECURITY:
--   - Tokens are stored as SHA-256 hashes, never as raw values.
--   - A leaked DB row cannot be used to verify an account.
--   - Tokens are single-use (consumed_at) and expire (expires_at).
-- =============================================================================

-- 1. Extend users with email verification state.
ALTER TABLE users
    ADD COLUMN email_verified    BOOLEAN     NOT NULL DEFAULT FALSE,
    ADD COLUMN email_verified_at TIMESTAMPTZ;

CREATE INDEX idx_users_email_verified ON users (email_verified);

-- 2. Option A backfill: grandfather all existing users as verified.
UPDATE users
   SET email_verified    = TRUE,
       email_verified_at = NOW()
 WHERE email_verified = FALSE;

-- 3. Verification tokens table.
--    One row per outstanding verification attempt. Multiple rows per user are
--    allowed (resends invalidate old tokens by marking them consumed), but
--    only one is ever usable because the verify endpoint marks it consumed.
--    `channel` reserves the field for Phase 3B's SMS OTP flow without a
--    schema change.
CREATE TABLE verification_tokens (
    id           BIGSERIAL       PRIMARY KEY,
    user_id      BIGINT          NOT NULL,
    token_hash   VARCHAR(128)    NOT NULL,
    channel      VARCHAR(20)     NOT NULL DEFAULT 'EMAIL',
    expires_at   TIMESTAMPTZ     NOT NULL,
    consumed_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_verification_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_verification_tokens_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_verification_tokens_user    ON verification_tokens (user_id, channel);
CREATE INDEX idx_verification_tokens_expires ON verification_tokens (expires_at);
