package org.skillbridge.repository;

import org.skillbridge.entity.Certificate;
import org.skillbridge.enums.CertificateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    Optional<Certificate> findByCertificateNumber(String certificateNumber);

    boolean existsByCertificateNumber(String certificateNumber);

    boolean existsByStudentRecordIdAndCourseId(Long studentRecordId, Long courseId);

    List<Certificate> findByStudentRecordIdOrderByIssuedAtDesc(Long studentRecordId);

    long countByStudentRecordIdAndStatus(Long studentRecordId, CertificateStatus status);
}