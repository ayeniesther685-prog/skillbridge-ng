package org.skillbridge.repository;

import org.skillbridge.entity.CourseLesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseLessonRepository extends JpaRepository<CourseLesson, Long> {
    List<CourseLesson> findByCourseIdOrderByOrderNumberAsc(Long courseId);
    long countByCourseId(Long courseId);
    boolean existsByCourseIdAndOrderNumber(Long courseId, Integer orderNumber);
}