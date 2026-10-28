CREATE TABLE conversations (
    id              BIGSERIAL       PRIMARY KEY,
    participant_a   BIGINT          NOT NULL,
    participant_b   BIGINT          NOT NULL,
    last_message_at TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_conversations_user_a FOREIGN KEY (participant_a) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_conversations_user_b FOREIGN KEY (participant_b) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT ck_conversations_order  CHECK (participant_a < participant_b),
    CONSTRAINT uq_conversations_pair   UNIQUE (participant_a, participant_b)
);

CREATE INDEX idx_conversations_user_a ON conversations (participant_a);
CREATE INDEX idx_conversations_user_b ON conversations (participant_b);
CREATE INDEX idx_conversations_last   ON conversations (last_message_at DESC NULLS LAST);

CREATE TABLE messages (
    id              BIGSERIAL       PRIMARY KEY,
    conversation_id BIGINT          NOT NULL,
    sender_id       BIGINT          NOT NULL,
    content         VARCHAR(4000)   NOT NULL,
    is_read         BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_sender       FOREIGN KEY (sender_id)       REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_messages_conversation ON messages (conversation_id, created_at DESC);
CREATE INDEX idx_messages_sender       ON messages (sender_id);
CREATE INDEX idx_messages_unread       ON messages (conversation_id, is_read) WHERE is_read = FALSE;