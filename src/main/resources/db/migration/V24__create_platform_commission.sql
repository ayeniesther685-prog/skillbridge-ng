-- Platform fee configuration (admin-editable rates)
CREATE TABLE platform_fee_config (
    id          BIGSERIAL       PRIMARY KEY,
    role_name   VARCHAR(30)     NOT NULL UNIQUE,
    fee_percent NUMERIC(5, 2)   NOT NULL,
    is_active   BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_fee_percent CHECK (fee_percent >= 0 AND fee_percent <= 100)
);

INSERT INTO platform_fee_config (role_name, fee_percent) VALUES
  ('FREELANCER', 5.00),
  ('STUDENT', 2.50);

-- Platform commissions ledger
CREATE TABLE platform_commissions (
    id              BIGSERIAL       PRIMARY KEY,
    contract_id     BIGINT          NOT NULL,
    amount_points   INTEGER         NOT NULL,
    fee_percent     NUMERIC(5, 2)   NOT NULL,
    contract_amount INTEGER         NOT NULL,
    freelancer_id   BIGINT          NOT NULL,
    status          VARCHAR(30)     NOT NULL DEFAULT 'RECORDED',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_commission_contract FOREIGN KEY (contract_id)
        REFERENCES contracts(id) ON DELETE CASCADE,
    CONSTRAINT fk_commission_freelancer FOREIGN KEY (freelancer_id)
        REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT ck_commission_amount CHECK (amount_points >= 0)
);

CREATE INDEX idx_commission_contract ON platform_commissions (contract_id);
CREATE INDEX idx_commission_created  ON platform_commissions (created_at DESC);

-- Treasury user (system account)
INSERT INTO users (first_name, last_name, email, password, is_active)
VALUES ('Platform', 'Treasury', 'treasury@skillbridge.ng', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true)
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'treasury@skillbridge.ng' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;