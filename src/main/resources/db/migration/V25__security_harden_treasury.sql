-- =============================================================================
-- V25 — Security hardening: neutralize the seeded treasury ADMIN account
-- =============================================================================
-- Context:
--   V24 seeded a treasury ADMIN user with a publicly known BCrypt hash
--   (`$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`).
--   That hash matches the string "password" — anyone with DB read access or
--   the repository history could log in as treasury and drain commissions.
--
-- This migration:
--   1. Replaces the hash with an unusable value that cannot be produced by
--      BCrypt for any input.
--   2. Sets is_active = false so the login path rejects it (defence in depth).
--   3. Leaves the ADMIN role assignment intact so historical FK references
--      (platform_commissions, created_by columns, etc.) stay valid.
--   4. Does NOT rotate the JWT secret — that is an out-of-band operational step.
--
-- Re-enabling for emergency access:
--   UPDATE users SET password = '<new-bcrypt-hash>', is_active = TRUE
--   WHERE email = 'treasury@skillbridge.ng';
-- =============================================================================

UPDATE users
   SET password  = '$2a$10$DISABLED_DISABLED_DISABLED_DISABLED_DISABLED_DISABLED',
       is_active = FALSE,
       updated_at = NOW()
 WHERE email = 'treasury@skillbridge.ng'
   AND password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';

-- Defensive: if for any reason the previous hash did not match (e.g. someone
-- already rotated it manually), still lock the account.
UPDATE users
   SET is_active = FALSE,
       updated_at = NOW()
 WHERE email = 'treasury@skillbridge.ng';