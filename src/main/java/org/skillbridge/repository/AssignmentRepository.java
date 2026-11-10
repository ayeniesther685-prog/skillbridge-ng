package org.skillbridge.repository;

import org.skillbridge.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourseIdAndPublishedTrueOrderByCreatedAtAsc(Long courseId);
    List<Assignment> findByCourseIdOrderByCreatedAtAsc(Long courseId);
    long countByCourseIdAndPublishedTrue(Long courseId);
    boolean existsByCourseIdAndTitle(Long courseId, String title);
}