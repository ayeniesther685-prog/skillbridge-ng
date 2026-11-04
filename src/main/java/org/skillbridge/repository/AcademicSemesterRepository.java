package org.skillbridge.repository;

import org.skillbridge.entity.AcademicSemester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcademicSemesterRepository extends JpaRepository<AcademicSemester, Long> {
    List<AcademicSemester> findBySessionIdOrderByStartDateAsc(Long sessionId);
}