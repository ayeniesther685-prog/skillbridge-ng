package org.skillbridge.dto.response;

import java.time.Instant;

public record LessonResponse(
        Long id,
        Long courseId,
        String title,
        String content,
        Integer orderNumber,
        Integer durationMinutes,
        String learningObjectives,
        String videoUrl,
        Instant createdAt,
        Instant updatedAt
) {}