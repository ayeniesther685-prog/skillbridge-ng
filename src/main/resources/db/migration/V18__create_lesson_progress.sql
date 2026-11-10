CREATE TABLE lesson_progress (
    id                BIGSERIAL       PRIMARY KEY,
    student_record_id BIGINT          NOT NULL,
    lesson_id         BIGINT          NOT NULL,
    status            VARCHAR(30)     NOT NULL DEFAULT 'NOT_STARTED',
    completed_at      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_progress_student FOREIGN KEY (student_record_id)
        REFERENCES student_records(id) ON DELETE CASCADE,
    CONSTRAINT fk_progress_lesson  FOREIGN KEY (lesson_id)
        REFERENCES course_lessons(id) ON DELETE CASCADE,
    CONSTRAINT uq_progress_student_lesson UNIQUE (student_record_id, lesson_id)
);

CREATE INDEX idx_progress_student ON lesson_progress (student_record_id);
CREATE INDEX idx_progress_lesson  ON lesson_progress (lesson_id);