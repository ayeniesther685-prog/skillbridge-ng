-- =============================================================================
-- V27 — Contract revision protection (Section 11 of the feature spec)
-- =============================================================================
-- Adds per-milestone revision tracking with a configurable limit (default 3).
-- Introduces a proper review cycle on milestones: SUBMITTED and
-- REVISION_REQUESTED states, so that a revision can only be requested after
-- the freelancer has actually delivered something.
--
-- Also adds forward-looking columns on `milestones` (approved_at,
-- review_deadline_at, released_at) that Phase 10 (payments/auto-release)
-- will use. They are nullable and unused by this phase.
-- =============================================================================

-- 1. Extend milestones with revision + review-cycle columns.
ALTER TABLE milestones
    ADD COLUMN revision_limit      INTEGER       NOT NULL DEFAULT 3,
    ADD COLUMN revision_count      INTEGER       NOT NULL DEFAULT 0,
    ADD COLUMN submitted_at        TIMESTAMPTZ,
    ADD COLUMN approved_at         TIMESTAMPTZ,
    ADD COLUMN review_deadline_at  TIMESTAMPTZ,
    ADD COLUMN released_at         TIMESTAMPTZ;

ALTER TABLE milestones
    ADD CONSTRAINT ck_milestones_revision_bounds
    CHECK (revision_count >= 0 AND revision_limit >= 0 AND revision_count <= revision_limit);


-- 2. Contract revisions table — one row per revision request.
--    `is_new_requirements` is TRUE when the client uses a "revision" as a
--    back door to ask for scope that was never in the original milestone.
--    When TRUE, the revision does NOT count against `revision_limit` and the
--    client must create a new milestone (or a new contract) instead. This
--    flag is set manually by the client in the request payload, and the
--    backend surfaces it to the freelancer so they can reject it.
CREATE TABLE contract_revisions (
    id                     BIGSERIAL       PRIMARY KEY,
    milestone_id           BIGINT          NOT NULL,
    requested_by           BIGINT          NOT NULL,
    reason                 VARCHAR(2000)   NOT NULL,
    status                 VARCHAR(30)     NOT NULL DEFAULT 'OPEN',
    is_new_requirements    BOOLEAN         NOT NULL DEFAULT FALSE,
    freelancer_response    VARCHAR(2000),
    resolved_at            TIMESTAMPTZ,
    created_at             TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_crev_milestone FOREIGN KEY (milestone_id)
        REFERENCES milestones(id) ON DELETE CASCADE,
    CONSTRAINT fk_crev_requester FOREIGN KEY (requested_by)
        REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_crev_milestone  ON contract_revisions (milestone_id, created_at DESC);
CREATE INDEX idx_crev_requester  ON contract_revisions (requested_by);
CREATE INDEX idx_crev_status     ON contract_revisions (status);
