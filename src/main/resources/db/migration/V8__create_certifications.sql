CREATE TABLE certifications (
    id                     BIGSERIAL       PRIMARY KEY,
    user_id                BIGINT          NOT NULL,
    name                   VARCHAR(200)    NOT NULL,
    issuing_organization   VARCHAR(200),
    issue_date             DATE,
    expiry_date            DATE,
    credential_url         VARCHAR(400),
    created_at             TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_certifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_certifications_user ON certifications (user_id);