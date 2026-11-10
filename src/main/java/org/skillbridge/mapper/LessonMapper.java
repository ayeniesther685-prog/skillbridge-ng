package org.skillbridge.mapper;

import org.skillbridge.dto.response.LessonProgressResponse;
import org.skillbridge.dto.response.LessonResponse;
import org.skillbridge.dto.response.LessonSummaryResponse;
import org.skillbridge.entity.CourseLesson;
import org.skillbridge.entity.LessonProgress;
import org.skillbridge.enums.LessonProgressStatus;
import org.springframework.stereotype.Component;

@Component
public class LessonMapper {

    public LessonSummaryResponse toSummary(CourseLesson l) {
        return new LessonSummaryResponse(
                l.getId(),
                l.getCourse().getId(),
                l.getTitle(),
                l.getOrderNumber(),
                l.getDurationMinutes(),
                l.getLearningObjectives(),
                l.getVideoUrl() != null && !l.getVideoUrl().isBlank()
        );
    }

    public LessonResponse toResponse(CourseLesson l) {
        return new LessonResponse(
                l.getId(),
                l.getCourse().getId(),
                l.getTitle(),
                l.getContent(),
                l.getOrderNumber(),
                l.getDurationMinutes(),
                l.getLearningObjectives(),
                l.getVideoUrl(),
                l.getCreatedAt(),
                l.getUpdatedAt()
        );
    }

    public LessonProgressResponse toProgressResponse(CourseLesson lesson, LessonProgress progress) {
        LessonProgressStatus status = progress == null
                ? LessonProgressStatus.NOT_STARTED
                : progress.getStatus();
        return new LessonProgressResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getOrderNumber(),
                status,
                progress == null ? null : progress.getCompletedAt()
        );
    }
}