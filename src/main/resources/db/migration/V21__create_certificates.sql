CREATE TABLE certificates (
    id                  BIGSERIAL       PRIMARY KEY,
    student_record_id   BIGINT          NOT NULL,
    course_id           BIGINT          NOT NULL,
    certificate_number  VARCHAR(50)     NOT NULL,
    final_score         INTEGER         NOT NULL DEFAULT 0,
    status              VARCHAR(30)     NOT NULL DEFAULT 'ISSUED',
    verification_hash   VARCHAR(128)    NOT NULL,
    issued_at           TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_certificates_number UNIQUE (certificate_number),
    CONSTRAINT uq_certificates_student_course UNIQUE (student_record_id, course_id),
    CONSTRAINT fk_certificates_student FOREIGN KEY (student_record_id)
        REFERENCES student_records(id) ON DELETE CASCADE,
    CONSTRAINT fk_certificates_course FOREIGN KEY (course_id)
        REFERENCES courses(id) ON DELETE RESTRICT,
    CONSTRAINT ck_certificates_score CHECK (final_score >= 0 AND final_score <= 100)
);

CREATE INDEX idx_certificates_number ON certificates (certificate_number);
CREATE INDEX idx_certificates_student ON certificates (student_record_id);
CREATE INDEX idx_certificates_course ON certificates (course_id);
CREATE INDEX idx_certificates_status ON certificates (status);