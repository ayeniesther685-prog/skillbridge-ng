package org.skillbridge.repository;

import org.skillbridge.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByDepartmentIdAndActiveTrueOrderByCodeAsc(Long departmentId);
    Optional<Course> findByCode(String code);
    boolean existsByCode(String code);
}