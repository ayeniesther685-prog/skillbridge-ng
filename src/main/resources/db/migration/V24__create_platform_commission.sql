-- The platform's own wallet — belongs to a system user
-- First, seed a system user (not a normal user, but a ledger for the platform)
INSERT INTO users (first_name, last_name, email, password, is_active)
VALUES ('Platform', 'Treasury', 'treasury@skillbridge.ng', 'REDACTED', true)
ON CONFLICT (email) DO NOTHING;

-- Give it ADMIN role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'treasury@skillbridge.ng' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Now every user gets a wallet, including treasury (handled by existing lazy-create code)

-- Commission tracking
CREATE TABLE platform_commissions (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    amount_kobo BIGINT NOT NULL,
    rate_percent NUMERIC(5,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_commission_contract FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE
);