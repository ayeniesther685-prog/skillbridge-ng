CREATE TABLE skills (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL,
    description VARCHAR(500),
    category    VARCHAR(80),
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_skills_name UNIQUE (name)
);

CREATE INDEX idx_skills_category ON skills (category);

CREATE TABLE user_skills (
    user_id  BIGINT   NOT NULL,
    skill_id BIGINT   NOT NULL,
    PRIMARY KEY (user_id, skill_id),
    CONSTRAINT fk_user_skills_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_skills_user  ON user_skills (user_id);
CREATE INDEX idx_user_skills_skill ON user_skills (skill_id);

INSERT INTO skills (name, category) VALUES
    ('Java',                'Programming'),
    ('Spring Boot',         'Programming'),
    ('JavaScript',          'Programming'),
    ('React',               'Frontend'),
    ('Node.js',             'Backend'),
    ('Python',              'Programming'),
    ('SQL',                 'Database'),
    ('PostgreSQL',          'Database'),
    ('Figma',               'Design'),
    ('UI/UX Design',        'Design'),
    ('Graphic Design',      'Design'),
    ('Digital Marketing',   'Marketing'),
    ('SEO',                 'Marketing'),
    ('Copywriting',         'Marketing'),
    ('Project Management',  'Business'),
    ('Data Analysis',       'Data'),
    ('Excel',               'Business'),
    ('Customer Service',    'Business'),
    ('Photography',         'Creative'),
    ('Video Editing',       'Creative'),
    ('Tailoring',           'Vocational'),
    ('Catering',            'Vocational'),
    ('Auto Mechanics',      'Vocational'),
    ('Plumbing',            'Vocational'),
    ('Electrical Wiring',   'Vocational');