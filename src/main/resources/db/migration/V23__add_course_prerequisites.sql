-- Add prerequisite column
ALTER TABLE courses ADD COLUMN prerequisite_course_id BIGINT
  REFERENCES courses(id) ON DELETE SET NULL;

CREATE INDEX idx_courses_prereq ON courses (prerequisite_course_id);

-- SWE201 requires SWE101
UPDATE courses
SET prerequisite_course_id = (SELECT id FROM courses WHERE code = 'SWE101')
WHERE code = 'SWE201';