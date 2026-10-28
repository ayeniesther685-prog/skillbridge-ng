package org.skillbridge.dto.response;

import java.time.Instant;

public record MessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String senderName,
        String content,
        boolean read,
        Instant createdAt
) {}