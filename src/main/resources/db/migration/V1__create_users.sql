CREATE TABLE users (
    id              BIGSERIAL       PRIMARY KEY,
    first_name      VARCHAR(100)    NOT NULL,
    last_name       VARCHAR(100)    NOT NULL,
    email           VARCHAR(180)    NOT NULL,
    password        VARCHAR(255)    NOT NULL,
    phone           VARCHAR(30),
    profile_picture VARCHAR(500),
    bio             VARCHAR(1000),
    location        VARCHAR(150),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_location ON users (location);
CREATE INDEX idx_users_active ON users (is_active);