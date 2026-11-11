-- Tier definitions (configurable, no redeploy needed to change limits)
CREATE TABLE ai_quota_tiers (
    id            BIGSERIAL       PRIMARY KEY,
    tier_name     VARCHAR(30)     NOT NULL,
    daily_limit   INTEGER         NOT NULL,
    monthly_limit INTEGER         NOT NULL,
    is_active     BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_ai_quota_tiers_name UNIQUE (tier_name),
    CONSTRAINT ck_ai_quota_tiers_positive CHECK (daily_limit >= 0 AND monthly_limit >= 0)
);

CREATE INDEX idx_ai_quota_tiers_active ON ai_quota_tiers (is_active);

-- Seed the three tiers
INSERT INTO ai_quota_tiers (tier_name, daily_limit, monthly_limit, is_active) VALUES
    ('FREE', 10, 100, TRUE),
    ('PRO', 50, 1000, TRUE),
    ('SPONSORED', 30, 500, TRUE);

-- Per-user quota state (one row per user)
CREATE TABLE user_ai_quotas (
    id                BIGSERIAL       PRIMARY KEY,
    user_id           BIGINT          NOT NULL,
    tier_id           BIGINT          NOT NULL,
    bonus_credits     INTEGER         NOT NULL DEFAULT 0,
    used_today        INTEGER         NOT NULL DEFAULT 0,
    used_this_month   INTEGER         NOT NULL DEFAULT 0,
    daily_reset_at    TIMESTAMPTZ     NOT NULL,
    monthly_reset_at  TIMESTAMPTZ     NOT NULL,
    created_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_ai_quotas_user UNIQUE (user_id),
    CONSTRAINT fk_user_ai_quotas_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_ai_quotas_tier FOREIGN KEY (tier_id)
        REFERENCES ai_quota_tiers(id) ON DELETE RESTRICT,
    CONSTRAINT ck_user_ai_quotas_nonneg CHECK (
        bonus_credits >= 0 AND used_today >= 0 AND used_this_month >= 0
    )
);

CREATE INDEX idx_user_ai_quotas_user ON user_ai_quotas (user_id);
CREATE INDEX idx_user_ai_quotas_tier ON user_ai_quotas (tier_id);

-- Audit log of every AI request attempt (success or rejected)
CREATE TABLE ai_usage_events (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    feature         VARCHAR(30)     NOT NULL,
    cost            INTEGER         NOT NULL DEFAULT 1,
    status          VARCHAR(30)     NOT NULL,
    tokens_used     INTEGER,
    error_message   VARCHAR(1000),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_ai_usage_events_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT ck_ai_usage_events_cost_positive CHECK (cost > 0)
);

CREATE INDEX idx_ai_usage_events_user    ON ai_usage_events (user_id, created_at DESC);
CREATE INDEX idx_ai_usage_events_feature ON ai_usage_events (feature);
CREATE INDEX idx_ai_usage_events_status  ON ai_usage_events (status);