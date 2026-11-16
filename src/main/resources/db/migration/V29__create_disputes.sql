-- =============================================================================
-- V29 — Dispute resolution (Phase 3D, Section 12 of the feature spec)
-- =============================================================================
-- Supports both contract-level and milestone-level disputes (Option C).
--   - contract_id is always set.
--   - milestone_id is optional (NULL = whole-contract dispute).
--
-- Rule: at most one OPEN dispute per contract OR per milestone at any time.
-- Enforced by partial unique indexes below (PostgreSQL supports
-- "WHERE status = 'OPEN'" in unique indexes).
--
-- Evidence: stores URLs/references only. Actual file upload to object storage
-- is Phase 13's scope. This is intentional and honest — no fake upload API.
--
-- No retention delete: disputes are legal records, kept indefinitely.
-- =============================================================================

-- 1. Contracts gain a disputed_at marker for fast "is this contract blocked?"
--    lookups without joining to disputes. Maintained by the dispute service.
ALTER TABLE contracts ADD COLUMN disputed_at TIMESTAMPTZ;
CREATE INDEX idx_contracts_disputed ON contracts (disputed_at) WHERE disputed_at IS NOT NULL;

-- 2. Disputes
CREATE TABLE disputes (
    id              BIGSERIAL       PRIMARY KEY,
    contract_id     BIGINT          NOT NULL,
    milestone_id    BIGINT,
    opened_by       BIGINT          NOT NULL,
    reason          VARCHAR(2000)   NOT NULL,
    status          VARCHAR(30)     NOT NULL DEFAULT 'OPEN',
    resolution      VARCHAR(2000),
    resolved_by     BIGINT,
    resolved_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_disputes_contract  FOREIGN KEY (contract_id)  REFERENCES contracts(id) ON DELETE CASCADE,
    CONSTRAINT fk_disputes_milestone FOREIGN KEY (milestone_id) REFERENCES milestones(id) ON DELETE CASCADE,
    CONSTRAINT fk_disputes_opener    FOREIGN KEY (opened_by)    REFERENCES users(id)     ON DELETE RESTRICT,
    CONSTRAINT fk_disputes_resolver  FOREIGN KEY (resolved_by)  REFERENCES users(id)     ON DELETE SET NULL
);

CREATE INDEX idx_disputes_contract ON disputes (contract_id, created_at DESC);
CREATE INDEX idx_disputes_milestone ON disputes (milestone_id) WHERE milestone_id IS NOT NULL;
CREATE INDEX idx_disputes_status ON disputes (status);
CREATE INDEX idx_disputes_opener ON disputes (opened_by);

-- Only one OPEN dispute per contract (covers whole-contract disputes)
CREATE UNIQUE INDEX uq_disputes_open_per_contract
    ON disputes (contract_id)
    WHERE status = 'OPEN';

-- Only one OPEN dispute per milestone
CREATE UNIQUE INDEX uq_disputes_open_per_milestone
    ON disputes (milestone_id)
    WHERE status = 'OPEN' AND milestone_id IS NOT NULL;

-- 3. Dispute evidence (URL references — no binary content stored in DB)
CREATE TABLE dispute_evidence (
    id              BIGSERIAL       PRIMARY KEY,
    dispute_id      BIGINT          NOT NULL,
    uploaded_by     BIGINT          NOT NULL,
    evidence_type   VARCHAR(30)     NOT NULL,
    url             VARCHAR(1000)   NOT NULL,
    description     VARCHAR(1000),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_dispute_evidence_dispute   FOREIGN KEY (dispute_id)  REFERENCES disputes(id) ON DELETE CASCADE,
    CONSTRAINT fk_dispute_evidence_uploader  FOREIGN KEY (uploaded_by) REFERENCES users(id)    ON DELETE RESTRICT,
    CONSTRAINT ck_dispute_evidence_url_notempty CHECK (length(trim(url)) > 0)
);

CREATE INDEX idx_dispute_evidence_dispute ON dispute_evidence (dispute_id, created_at DESC);

-- 4. Dispute messages (thread between parties + admin)
CREATE TABLE dispute_messages (
    id              BIGSERIAL       PRIMARY KEY,
    dispute_id      BIGINT          NOT NULL,
    author_id       BIGINT          NOT NULL,
    message         VARCHAR(4000)   NOT NULL,
    is_from_admin   BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_dispute_messages_dispute FOREIGN KEY (dispute_id) REFERENCES disputes(id) ON DELETE CASCADE,
    CONSTRAINT fk_dispute_messages_author  FOREIGN KEY (author_id)  REFERENCES users(id)    ON DELETE RESTRICT
);

CREATE INDEX idx_dispute_messages_dispute ON dispute_messages (dispute_id, created_at ASC);
