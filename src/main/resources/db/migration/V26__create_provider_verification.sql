-- =============================================================================
-- V26 — Provider verification (Section 4 of the SkillBridge feature spec)
-- =============================================================================
-- Adds an explicit verification profile for users who act as employers,
-- recruitment agencies, or training providers. Keeps the existing CLIENT role
-- and adds no new role. Verification status gates *new* provider activities
-- (posting jobs) — existing accounts, jobs, and relationships are untouched.
-- =============================================================================

CREATE TABLE provider_verifications (
    id                     BIGSERIAL       PRIMARY KEY,
    user_id                BIGINT          NOT NULL,
    provider_type          VARCHAR(30)     NOT NULL,
    legal_name             VARCHAR(200)    NOT NULL,
    cac_registration_number VARCHAR(50),
    official_email         VARCHAR(180)    NOT NULL,
    official_phone         VARCHAR(30)     NOT NULL,
    physical_address       VARCHAR(500)    NOT NULL,
    website                VARCHAR(300),
    linkedin_url           VARCHAR(300),
    authorized_rep_name    VARCHAR(200)    NOT NULL,
    authorized_rep_role    VARCHAR(100)    NOT NULL,
    status                 VARCHAR(30)     NOT NULL DEFAULT 'UNVERIFIED',
    submitted_at           TIMESTAMPTZ,
    verified_at            TIMESTAMPTZ,
    verified_by_admin_id   BIGINT,
    expires_at             TIMESTAMPTZ,
    admin_notes            VARCHAR(2000),
    last_recheck_at        TIMESTAMPTZ,
    created_at             TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_provider_verifications_user UNIQUE (user_id),
    CONSTRAINT fk_provider_verifications_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_provider_verifications_admin FOREIGN KEY (verified_by_admin_id)
        REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_provider_verifications_status   ON provider_verifications (status);
CREATE INDEX idx_provider_verifications_type     ON provider_verifications (provider_type);
CREATE INDEX idx_provider_verifications_updated  ON provider_verifications (updated_at DESC);

CREATE TABLE provider_verification_documents (
    id                     BIGSERIAL       PRIMARY KEY,
    verification_id        BIGINT          NOT NULL,
    document_type          VARCHAR(50)     NOT NULL,
    file_reference         VARCHAR(500)    NOT NULL,
    original_filename      VARCHAR(255)    NOT NULL,
    mime_type              VARCHAR(100),
    size_bytes             BIGINT,
    uploaded_at            TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_pv_documents_verification FOREIGN KEY (verification_id)
        REFERENCES provider_verifications(id) ON DELETE CASCADE
);

CREATE INDEX idx_pv_documents_verification ON provider_verification_documents (verification_id);

CREATE TABLE provider_verification_events (
    id                     BIGSERIAL       PRIMARY KEY,
    verification_id        BIGINT          NOT NULL,
    event_type             VARCHAR(50)     NOT NULL,
    actor_user_id          BIGINT,
    from_status            VARCHAR(30),
    to_status              VARCHAR(30),
    notes                  VARCHAR(2000),
    created_at             TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_pv_events_verification FOREIGN KEY (verification_id)
        REFERENCES provider_verifications(id) ON DELETE CASCADE,
    CONSTRAINT fk_pv_events_actor FOREIGN KEY (actor_user_id)
        REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_pv_events_verification ON provider_verification_events (verification_id, created_at DESC);
CREATE INDEX idx_pv_events_type         ON provider_verification_events (event_type);

-- Backfill: any user who currently holds the CLIENT role and does not yet
-- have a verification row starts as UNVERIFIED. This is non-destructive —
-- no jobs, applications, or contracts are touched.
INSERT INTO provider_verifications (
    user_id, provider_type, legal_name, official_email, official_phone,
    physical_address, authorized_rep_name, authorized_rep_role, status
)
SELECT
    u.id,
    'EMPLOYER',
    COALESCE(NULLIF(TRIM(u.first_name || ' ' || u.last_name), ''), 'Unspecified'),
    u.email,
    COALESCE(u.phone, 'not-provided'),
    'not-provided',
    COALESCE(NULLIF(TRIM(u.first_name || ' ' || u.last_name), ''), 'Unspecified'),
    'Owner',
    'UNVERIFIED'
FROM users u
JOIN user_roles ur ON ur.user_id = u.id
JOIN roles r       ON r.id = ur.role_id
WHERE r.name = 'CLIENT'
  AND NOT EXISTS (SELECT 1 FROM provider_verifications pv WHERE pv.user_id = u.id);