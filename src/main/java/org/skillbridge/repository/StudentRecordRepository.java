package org.skillbridge.repository;

import org.skillbridge.entity.StudentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRecordRepository extends JpaRepository<StudentRecord, Long> {
    Optional<StudentRecord> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    boolean existsByStudentId(String studentId);
    long countByProgramId(Long programId);
}