CREATE TABLE profiles (
    id           BIGSERIAL       PRIMARY KEY,
    user_id      BIGINT          NOT NULL,
    headline     VARCHAR(200),
    website      VARCHAR(300),
    linkedin_url VARCHAR(300),
    github_url   VARCHAR(300),
    twitter_url  VARCHAR(300),
    availability VARCHAR(50),
    hourly_rate  NUMERIC(12, 2),
    created_at   TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_profiles_user UNIQUE (user_id),
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_profiles_user ON profiles (user_id);