-- 1. Wallets (one per user)
CREATE TABLE wallets (
    id             BIGSERIAL       PRIMARY KEY,
    user_id        BIGINT          NOT NULL,
    balance_points INTEGER         NOT NULL DEFAULT 0,
    currency       VARCHAR(10)     NOT NULL DEFAULT 'PTS',
    created_at     TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_wallets_user UNIQUE (user_id),
    CONSTRAINT fk_wallets_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT ck_wallets_balance_nonneg CHECK (balance_points >= 0)
);

CREATE INDEX idx_wallets_user ON wallets (user_id);

-- 2. Wallet transactions (append-only ledger)
CREATE TABLE wallet_transactions (
    id             BIGSERIAL       PRIMARY KEY,
    wallet_id      BIGINT          NOT NULL,
    type           VARCHAR(40)     NOT NULL,
    amount_points  INTEGER         NOT NULL,
    balance_after  INTEGER         NOT NULL,
    reference      VARCHAR(100),
    description    VARCHAR(500),
    created_at     TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_wallet_tx_wallet FOREIGN KEY (wallet_id)
        REFERENCES wallets(id) ON DELETE CASCADE
);

CREATE INDEX idx_wallet_tx_wallet ON wallet_transactions (wallet_id, created_at DESC);
CREATE INDEX idx_wallet_tx_type   ON wallet_transactions (type);

-- 3. Withdrawal requests (PENDING → APPROVED → PAID)
CREATE TABLE withdrawal_requests (
    id              BIGSERIAL       PRIMARY KEY,
    wallet_id       BIGINT          NOT NULL,
    amount_points   INTEGER         NOT NULL,
    status          VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    bank_name       VARCHAR(100),
    account_number  VARCHAR(30),
    account_name    VARCHAR(200),
    admin_notes     VARCHAR(1000),
    requested_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    processed_at    TIMESTAMPTZ,
    CONSTRAINT fk_withdrawal_wallet FOREIGN KEY (wallet_id)
        REFERENCES wallets(id) ON DELETE CASCADE,
    CONSTRAINT ck_withdrawal_amount CHECK (amount_points > 0)
);

CREATE INDEX idx_withdrawal_wallet  ON withdrawal_requests (wallet_id);
CREATE INDEX idx_withdrawal_status  ON withdrawal_requests (status);

-- 4. Daily challenges
CREATE TABLE daily_challenges (
    id              BIGSERIAL       PRIMARY KEY,
    title           VARCHAR(200)    NOT NULL,
    description     TEXT            NOT NULL,
    point_reward    INTEGER         NOT NULL,
    difficulty      VARCHAR(20)     NOT NULL DEFAULT 'BEGINNER',
    max_winners     INTEGER,
    winners_count   INTEGER         NOT NULL DEFAULT 0,
    active_from     TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    active_until    TIMESTAMPTZ     NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_challenge_creator FOREIGN KEY (created_by)
        REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT ck_challenge_reward CHECK (point_reward > 0)
);

CREATE INDEX idx_challenges_active ON daily_challenges (is_active, active_until);

-- 5. Challenge submissions
CREATE TABLE challenge_submissions (
    id              BIGSERIAL       PRIMARY KEY,
    challenge_id    BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    content         TEXT            NOT NULL,
    status          VARCHAR(30)     NOT NULL DEFAULT 'SUBMITTED',
    points_awarded  INTEGER         NOT NULL DEFAULT 0,
    admin_feedback  VARCHAR(2000),
    reviewed_by     BIGINT,
    reviewed_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_challenge_submission UNIQUE (challenge_id, user_id),
    CONSTRAINT fk_submission_challenge FOREIGN KEY (challenge_id)
        REFERENCES daily_challenges(id) ON DELETE CASCADE,
    CONSTRAINT fk_submission_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_submission_reviewer FOREIGN KEY (reviewed_by)
        REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_submissions_challenge ON challenge_submissions (challenge_id);
CREATE INDEX idx_submissions_user      ON challenge_submissions (user_id);

-- 6. Job funding (structure only, real payment deferred)
CREATE TABLE job_fundings (
    id              BIGSERIAL       PRIMARY KEY,
    job_id          BIGINT          NOT NULL,
    funded_by       BIGINT          NOT NULL,
    amount_points   INTEGER         NOT NULL,
    status          VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    notes           VARCHAR(1000),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_funding_job  FOREIGN KEY (job_id)
        REFERENCES jobs(id) ON DELETE CASCADE,
    CONSTRAINT fk_funding_user FOREIGN KEY (funded_by)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT ck_funding_amount CHECK (amount_points > 0)
);

CREATE INDEX idx_funding_job  ON job_fundings (job_id);
CREATE INDEX idx_funding_user ON job_fundings (funded_by);

-- Seed: 3 demo challenges
INSERT INTO daily_challenges (title, description, point_reward, difficulty, max_winners, active_until)
VALUES
  ('FizzBuzz Challenge',
   'Write FizzBuzz in any programming language. Paste your code as the submission.',
   50, 'BEGINNER', 10, NOW() + INTERVAL '7 days'),
  ('Explain Polymorphism',
   'In 2 sentences, explain Java polymorphism in your own words.',
   30, 'INTERMEDIATE', 20, NOW() + INTERVAL '7 days'),
  ('Fix the Bug',
   'A function returns the wrong sum. Describe what is wrong and how to fix it.',
   75, 'INTERMEDIATE', 10, NOW() + INTERVAL '7 days');