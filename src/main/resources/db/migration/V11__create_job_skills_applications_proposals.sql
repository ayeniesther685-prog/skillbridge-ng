-- Job ↔ Skill many-to-many
CREATE TABLE job_skills (
    job_id   BIGINT   NOT NULL,
    skill_id BIGINT   NOT NULL,
    PRIMARY KEY (job_id, skill_id),
    CONSTRAINT fk_job_skills_job   FOREIGN KEY (job_id)   REFERENCES jobs(id)   ON DELETE CASCADE,
    CONSTRAINT fk_job_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);

CREATE INDEX idx_job_skills_job   ON job_skills (job_id);
CREATE INDEX idx_job_skills_skill ON job_skills (skill_id);

-- Applications
CREATE TABLE applications (
    id              BIGSERIAL       PRIMARY KEY,
    job_id          BIGINT          NOT NULL,
    applicant_id    BIGINT          NOT NULL,
    expected_budget NUMERIC(12, 2),
    status          VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_applications_job       FOREIGN KEY (job_id)       REFERENCES jobs(id)  ON DELETE CASCADE,
    CONSTRAINT fk_applications_applicant FOREIGN KEY (applicant_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_applications_job_applicant UNIQUE (job_id, applicant_id)
);

CREATE INDEX idx_applications_job       ON applications (job_id);
CREATE INDEX idx_applications_applicant ON applications (applicant_id);
CREATE INDEX idx_applications_status    ON applications (status);

-- Proposals (1:1 with application)
CREATE TABLE proposals (
    id                       BIGSERIAL       PRIMARY KEY,
    application_id           BIGINT          NOT NULL,
    cover_letter             VARCHAR(4000)   NOT NULL,
    proposed_price           NUMERIC(12, 2)  NOT NULL,
    estimated_delivery_days  INTEGER,
    created_at               TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_proposals_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE,
    CONSTRAINT uq_proposals_application UNIQUE (application_id)
);

CREATE INDEX idx_proposals_application ON proposals (application_id);