CREATE TABLE work_experience (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    company         VARCHAR(200)    NOT NULL,
    position        VARCHAR(150)    NOT NULL,
    description     VARCHAR(1000),
    start_date      DATE,
    end_date        DATE,
    is_current      BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_work_experience_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_work_experience_user ON work_experience (user_id);