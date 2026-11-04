package org.skillbridge.repository;

import org.skillbridge.entity.AcademicSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademicSessionRepository extends JpaRepository<AcademicSession, Long> {
    Optional<AcademicSession> findByCurrentTrue();
    Optional<AcademicSession> findByName(String name);
    boolean existsByName(String name);
}