package org.skillbridge.repository;

import org.skillbridge.entity.LessonProgress;
import org.skillbridge.enums.LessonProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByStudentRecordIdAndLessonId(Long studentRecordId, Long lessonId);
    List<LessonProgress> findByStudentRecordIdAndLessonCourseId(Long studentRecordId, Long courseId);
    long countByStudentRecordIdAndLessonCourseIdAndStatus(
            Long studentRecordId, Long courseId, LessonProgressStatus status);
}