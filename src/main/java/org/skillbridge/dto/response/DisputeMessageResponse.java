package org.skillbridge.dto.response;

import java.time.Instant;

public record DisputeMessageResponse(
        Long id,
        Long authorId,
        String authorName,
        String message,
        boolean isFromAdmin,
        Instant createdAt
) {}
