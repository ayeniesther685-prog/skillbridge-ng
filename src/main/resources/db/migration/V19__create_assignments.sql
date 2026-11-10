CREATE TABLE assignments (
    id              BIGSERIAL       PRIMARY KEY,
    course_id       BIGINT          NOT NULL,
    title           VARCHAR(200)    NOT NULL,
    description     VARCHAR(2000),
    instructions    TEXT,
    max_score       INTEGER         NOT NULL DEFAULT 100,
    weight          NUMERIC(5, 2)   NOT NULL DEFAULT 0.00,
    due_date        DATE,
    is_published    BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_assignments_course FOREIGN KEY (course_id)
        REFERENCES courses(id) ON DELETE CASCADE
);

CREATE INDEX idx_assignments_course ON assignments (course_id);
CREATE INDEX idx_assignments_published ON assignments (is_published);

CREATE TABLE assignment_submissions (
    id                BIGSERIAL       PRIMARY KEY,
    assignment_id     BIGINT          NOT NULL,
    student_record_id BIGINT          NOT NULL,
    content           TEXT,
    file_url          VARCHAR(500),
    status            VARCHAR(30)     NOT NULL DEFAULT 'SUBMITTED',
    score             INTEGER,
    feedback          VARCHAR(4000),
    submitted_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    graded_at         TIMESTAMPTZ,
    graded_by         BIGINT,
    updated_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_submissions_assignment FOREIGN KEY (assignment_id)
        REFERENCES assignments(id) ON DELETE CASCADE,
    CONSTRAINT fk_submissions_student FOREIGN KEY (student_record_id)
        REFERENCES student_records(id) ON DELETE CASCADE,
    CONSTRAINT fk_submissions_grader FOREIGN KEY (graded_by)
        REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT uq_submissions_assignment_student UNIQUE (assignment_id, student_record_id)
);

CREATE INDEX idx_submissions_assignment ON assignment_submissions (assignment_id);
CREATE INDEX idx_submissions_student ON assignment_submissions (student_record_id);
CREATE INDEX idx_submissions_status ON assignment_submissions (status);

-- Seed one sample assignment for SWE101
INSERT INTO assignments (course_id, title, description, instructions, max_score, weight, due_date, is_published)
VALUES (
    (SELECT id FROM courses WHERE code = 'SWE101'),
    'FizzBuzz in Java',
    'Write a program that prints the numbers 1 to 100, using FizzBuzz rules.',
    E'Create a class called FizzBuzz with a main method.\n\nFor numbers 1 to 100:\n- Print "Fizz" if the number is divisible by 3\n- Print "Buzz" if the number is divisible by 5\n- Print "FizzBuzz" if the number is divisible by both\n- Otherwise print the number\n\nSubmit a single Java file (FizzBuzz.java) or paste the code into the submission content.',
    100,
    10.00,
    '2026-12-15',
    TRUE
);