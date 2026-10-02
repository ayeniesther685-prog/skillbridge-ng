CREATE TABLE jobs (
    id                BIGSERIAL       PRIMARY KEY,
    client_id         BIGINT          NOT NULL,
    title             VARCHAR(200)    NOT NULL,
    description       VARCHAR(4000)   NOT NULL,
    budget_min        NUMERIC(12, 2),
    budget_max        NUMERIC(12, 2),
    location          VARCHAR(150),
    job_type          VARCHAR(30)     NOT NULL,
    experience_level  VARCHAR(30)     NOT NULL,
    status            VARCHAR(30)     NOT NULL DEFAULT 'OPEN',
    deadline          DATE,
    created_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_jobs_client FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_jobs_client       ON jobs (client_id);
CREATE INDEX idx_jobs_status       ON jobs (status);
CREATE INDEX idx_jobs_location     ON jobs (location);
CREATE INDEX idx_jobs_job_type     ON jobs (job_type);
CREATE INDEX idx_jobs_created_at   ON jobs (created_at DESC);