package org.skillbridge.repository;

import org.skillbridge.entity.Application;
import org.skillbridge.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByJobIdAndApplicantId(Long jobId, Long applicantId);

    boolean existsByJobIdAndApplicantId(Long jobId, Long applicantId);

    Page<Application> findByApplicantId(Long applicantId, Pageable pageable);

    Page<Application> findByJobClientId(Long clientId, Pageable pageable);

    Page<Application> findByJobId(Long jobId, Pageable pageable);

    long countByJobIdAndStatus(Long jobId, ApplicationStatus status);
}