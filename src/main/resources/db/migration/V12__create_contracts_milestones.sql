CREATE TABLE contracts (
    id             BIGSERIAL       PRIMARY KEY,
    job_id         BIGINT          NOT NULL,
    client_id      BIGINT          NOT NULL,
    freelancer_id  BIGINT          NOT NULL,
    title          VARCHAR(200)    NOT NULL,
    description    VARCHAR(4000),
    amount         NUMERIC(12, 2)  NOT NULL,
    start_date     DATE,
    end_date       DATE,
    status         VARCHAR(30)     NOT NULL DEFAULT 'DRAFT',
    created_at     TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_contracts_job        FOREIGN KEY (job_id)        REFERENCES jobs(id)  ON DELETE CASCADE,
    CONSTRAINT fk_contracts_client     FOREIGN KEY (client_id)     REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_contracts_freelancer FOREIGN KEY (freelancer_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT uq_contracts_job UNIQUE (job_id)
);

CREATE INDEX idx_contracts_client     ON contracts (client_id);
CREATE INDEX idx_contracts_freelancer ON contracts (freelancer_id);
CREATE INDEX idx_contracts_status     ON contracts (status);

CREATE TABLE milestones (
    id            BIGSERIAL       PRIMARY KEY,
    contract_id   BIGINT          NOT NULL,
    title         VARCHAR(200)    NOT NULL,
    description   VARCHAR(2000),
    amount        NUMERIC(12, 2)  NOT NULL,
    due_date      DATE,
    status        VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    completed_at  TIMESTAMPTZ,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_milestones_contract FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE
);

CREATE INDEX idx_milestones_contract ON milestones (contract_id);
CREATE INDEX idx_milestones_status   ON milestones (status);