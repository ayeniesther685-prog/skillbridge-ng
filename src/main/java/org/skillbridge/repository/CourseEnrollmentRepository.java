package org.skillbridge.repository;

import org.skillbridge.entity.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    List<CourseEnrollment> findByStudentRecordIdOrderByEnrolledAtDesc(Long studentRecordId);
    boolean existsByStudentRecordIdAndCourseIdAndSemesterId(
            Long studentRecordId, Long courseId, Long semesterId);
}