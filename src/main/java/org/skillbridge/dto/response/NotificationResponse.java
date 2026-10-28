package org.skillbridge.dto.response;

import org.skillbridge.enums.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long userId,
        String title,
        String message,
        NotificationType type,
        boolean read,
        Instant createdAt
) {}