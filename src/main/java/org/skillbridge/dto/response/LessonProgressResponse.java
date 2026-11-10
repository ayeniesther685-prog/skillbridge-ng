package org.skillbridge.dto.response;

import org.skillbridge.enums.LessonProgressStatus;

import java.time.Instant;

public record LessonProgressResponse(
        Long lessonId,
        String lessonTitle,
        Integer orderNumber,
        LessonProgressStatus status,
        Instant completedAt
) {}