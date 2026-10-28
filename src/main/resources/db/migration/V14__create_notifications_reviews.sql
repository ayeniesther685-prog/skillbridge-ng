CREATE TABLE notifications (
    id          BIGSERIAL       PRIMARY KEY,
    user_id     BIGINT          NOT NULL,
    title       VARCHAR(200)    NOT NULL,
    message     VARCHAR(1000)   NOT NULL,
    type        VARCHAR(30)     NOT NULL,
    is_read     BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user     ON notifications (user_id, created_at DESC);
CREATE INDEX idx_notifications_unread   ON notifications (user_id, is_read) WHERE is_read = FALSE;

CREATE TABLE reviews (
    id           BIGSERIAL       PRIMARY KEY,
    reviewer_id  BIGINT          NOT NULL,
    reviewee_id  BIGINT          NOT NULL,
    job_id       BIGINT          NOT NULL,
    rating       SMALLINT        NOT NULL,
    comment      VARCHAR(2000),
    created_at   TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_reviewee FOREIGN KEY (reviewee_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_job      FOREIGN KEY (job_id)      REFERENCES jobs(id)  ON DELETE CASCADE,
    CONSTRAINT ck_reviews_rating   CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT uq_reviews_reviewer_job UNIQUE (reviewer_id, job_id)
);

CREATE INDEX idx_reviews_reviewee ON reviews (reviewee_id);
CREATE INDEX idx_reviews_job      ON reviews (job_id);