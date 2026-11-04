-- Academic programs (top-level: e.g. "Software Engineering", "Data Science")
CREATE TABLE academic_programs (
    id               BIGSERIAL       PRIMARY KEY,
    name             VARCHAR(200)    NOT NULL,
    code             VARCHAR(30)     NOT NULL,
    description      VARCHAR(2000),
    duration_months  INTEGER,
    is_active        BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_academic_programs_code UNIQUE (code)
);

CREATE INDEX idx_academic_programs_active ON academic_programs (is_active);

-- Departments within programs (e.g. "Backend Engineering" under "Software Engineering")
CREATE TABLE academic_departments (
    id           BIGSERIAL       PRIMARY KEY,
    program_id   BIGINT          NOT NULL,
    name         VARCHAR(200)    NOT NULL,
    code         VARCHAR(30)     NOT NULL,
    description  VARCHAR(2000),
    created_at   TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_academic_departments_code UNIQUE (code),
    CONSTRAINT fk_academic_departments_program FOREIGN KEY (program_id)
        REFERENCES academic_programs(id) ON DELETE CASCADE
);

CREATE INDEX idx_academic_departments_program ON academic_departments (program_id);

-- Academic sessions (e.g. "2025/2026")
CREATE TABLE academic_sessions (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(50)     NOT NULL,
    start_date  DATE            NOT NULL,
    end_date    DATE            NOT NULL,
    is_current  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_academic_sessions_name UNIQUE (name)
);

CREATE INDEX idx_academic_sessions_current ON academic_sessions (is_current);

-- Semesters within a session
CREATE TABLE academic_semesters (
    id          BIGSERIAL       PRIMARY KEY,
    session_id  BIGINT          NOT NULL,
    name        VARCHAR(50)     NOT NULL,
    start_date  DATE            NOT NULL,
    end_date    DATE            NOT NULL,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_academic_semesters_session FOREIGN KEY (session_id)
        REFERENCES academic_sessions(id) ON DELETE CASCADE
);

CREATE INDEX idx_academic_semesters_session ON academic_semesters (session_id);

-- Courses (the catalog)
CREATE TABLE courses (
    id             BIGSERIAL       PRIMARY KEY,
    department_id  BIGINT          NOT NULL,
    code           VARCHAR(30)     NOT NULL,
    title          VARCHAR(200)    NOT NULL,
    description    VARCHAR(4000),
    level          VARCHAR(20)     NOT NULL DEFAULT 'BEGINNER',
    credits        INTEGER         NOT NULL DEFAULT 3,
    duration_hours INTEGER,
    is_active      BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_courses_code UNIQUE (code),
    CONSTRAINT fk_courses_department FOREIGN KEY (department_id)
        REFERENCES academic_departments(id) ON DELETE RESTRICT
);

CREATE INDEX idx_courses_department ON courses (department_id);
CREATE INDEX idx_courses_level      ON courses (level);

-- Student records (one per enrolled user)
CREATE TABLE student_records (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    student_id      VARCHAR(30)     NOT NULL,
    program_id      BIGINT          NOT NULL,
    department_id   BIGINT,
    admission_date  DATE            NOT NULL,
    status          VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_student_records_user    UNIQUE (user_id),
    CONSTRAINT uq_student_records_sid     UNIQUE (student_id),
    CONSTRAINT fk_student_records_user    FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_records_program FOREIGN KEY (program_id)
        REFERENCES academic_programs(id) ON DELETE RESTRICT,
    CONSTRAINT fk_student_records_dept    FOREIGN KEY (department_id)
        REFERENCES academic_departments(id) ON DELETE SET NULL
);

CREATE INDEX idx_student_records_program ON student_records (program_id);
CREATE INDEX idx_student_records_status  ON student_records (status);

-- Course enrollments (student takes course in a specific semester)
CREATE TABLE course_enrollments (
    id               BIGSERIAL       PRIMARY KEY,
    student_record_id BIGINT         NOT NULL,
    course_id        BIGINT          NOT NULL,
    semester_id      BIGINT          NOT NULL,
    status           VARCHAR(30)     NOT NULL DEFAULT 'ENROLLED',
    final_grade      VARCHAR(5),
    enrolled_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_record_id)
        REFERENCES student_records(id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_course  FOREIGN KEY (course_id)
        REFERENCES courses(id) ON DELETE RESTRICT,
    CONSTRAINT fk_enrollments_semester FOREIGN KEY (semester_id)
        REFERENCES academic_semesters(id) ON DELETE RESTRICT,
    CONSTRAINT uq_enrollments_triple UNIQUE (student_record_id, course_id, semester_id)
);

CREATE INDEX idx_enrollments_student  ON course_enrollments (student_record_id);
CREATE INDEX idx_enrollments_course   ON course_enrollments (course_id);
CREATE INDEX idx_enrollments_semester ON course_enrollments (semester_id);

-- Seed: one program, one department, one session, one semester, two courses
INSERT INTO academic_programs (name, code, description, duration_months) VALUES
    ('Software Engineering', 'SWE', 'Practical software engineering program', 24);

INSERT INTO academic_departments (program_id, name, code, description) VALUES
    ((SELECT id FROM academic_programs WHERE code = 'SWE'),
     'Backend Engineering', 'SWE-BE',
     'Server-side development, APIs, databases');

INSERT INTO academic_sessions (name, start_date, end_date, is_current) VALUES
    ('2025/2026', '2025-09-01', '2026-07-31', TRUE);

INSERT INTO academic_semesters (session_id, name, start_date, end_date) VALUES
    ((SELECT id FROM academic_sessions WHERE name = '2025/2026'),
     'First Semester', '2025-09-01', '2025-12-20');

INSERT INTO courses (department_id, code, title, description, level, credits, duration_hours) VALUES
    ((SELECT id FROM academic_departments WHERE code = 'SWE-BE'),
     'SWE101', 'Introduction to Java',
     'Java fundamentals, OOP, and the Spring ecosystem',
     'BEGINNER', 3, 60),
    ((SELECT id FROM academic_departments WHERE code = 'SWE-BE'),
     'SWE201', 'REST APIs with Spring Boot',
     'Building production-grade REST APIs',
     'INTERMEDIATE', 3, 60);