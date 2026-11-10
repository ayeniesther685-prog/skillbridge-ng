package org.skillbridge.dto.response;

public record LessonSummaryResponse(
        Long id,
        Long courseId,
        String title,
        Integer orderNumber,
        Integer durationMinutes,
        String learningObjectives,
        boolean hasVideo
) {}