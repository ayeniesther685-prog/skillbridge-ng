CREATE TABLE roles (
    id          SMALLSERIAL     PRIMARY KEY,
    name        VARCHAR(30)     NOT NULL,
    CONSTRAINT uq_roles_name UNIQUE (name)
);

INSERT INTO roles (name) VALUES
    ('STUDENT'),
    ('FREELANCER'),
    ('CLIENT'),
    ('ADMIN');

CREATE TABLE user_roles (
    user_id     BIGINT          NOT NULL,
    role_id     SMALLINT        NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);

CREATE INDEX idx_user_roles_user ON user_roles (user_id);
CREATE INDEX idx_user_roles_role ON user_roles (role_id);