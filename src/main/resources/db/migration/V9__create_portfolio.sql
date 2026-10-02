CREATE TABLE portfolio_items (
    id           BIGSERIAL       PRIMARY KEY,
    user_id      BIGINT          NOT NULL,
    title        VARCHAR(200)    NOT NULL,
    description  VARCHAR(2000),
    image_url    VARCHAR(500),
    project_url  VARCHAR(500),
    technologies VARCHAR(500),
    created_at   TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_portfolio_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_portfolio_user ON portfolio_items (user_id);