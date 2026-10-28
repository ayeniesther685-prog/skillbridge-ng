package org.skillbridge.dto.response;

import java.time.Instant;

public record ConversationResponse(
        Long id,
        Long otherUserId,
        String otherUserName,
        String otherUserEmail,
        String otherUserPicture,
        Instant lastMessageAt,
        long unreadCount,
        Instant createdAt
) {}