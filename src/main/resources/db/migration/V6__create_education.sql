CREATE TABLE education (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    institution     VARCHAR(200)    NOT NULL,
    degree          VARCHAR(150),
    field_of_study  VARCHAR(150),
    start_date      DATE,
    end_date        DATE,
    description     VARCHAR(1000),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_education_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_education_user ON education (user_id);