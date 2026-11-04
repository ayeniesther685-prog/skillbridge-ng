package org.skillbridge.repository;

import org.skillbridge.entity.AcademicProgram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademicProgramRepository extends JpaRepository<AcademicProgram, Long> {
    List<AcademicProgram> findByActiveTrueOrderByNameAsc();
    Optional<AcademicProgram> findByCode(String code);
    boolean existsByCode(String code);
}