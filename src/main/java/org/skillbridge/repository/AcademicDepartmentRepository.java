package org.skillbridge.repository;

import org.skillbridge.entity.AcademicDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademicDepartmentRepository extends JpaRepository<AcademicDepartment, Long> {
    List<AcademicDepartment> findByProgramIdOrderByNameAsc(Long programId);
    Optional<AcademicDepartment> findByCode(String code);
    boolean existsByCode(String code);
}