package org.skillbridge.dto.response;

import java.util.List;

public record CourseProgressResponse(
        Long courseId,
        String courseTitle,
        long totalLessons,
        long completedLessons,
        int percentComplete,
        List<LessonProgressResponse> lessons
) {}