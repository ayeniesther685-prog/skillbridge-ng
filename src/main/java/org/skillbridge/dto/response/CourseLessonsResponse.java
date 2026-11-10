package org.skillbridge.dto.response;

import java.util.List;

public record CourseLessonsResponse(
        Long courseId,
        String courseTitle,
        long totalLessons,
        List<LessonSummaryResponse> lessons
) {}