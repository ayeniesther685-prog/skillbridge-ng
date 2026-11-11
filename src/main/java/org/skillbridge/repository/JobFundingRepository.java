package org.skillbridge.repository;

import org.skillbridge.entity.JobFunding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobFundingRepository extends JpaRepository<JobFunding, Long> {
    Optional<JobFunding> findByJobId(Long jobId);
    List<JobFunding> findByJobIdOrderByCreatedAtDesc(Long jobId);
    List<JobFunding> findByFundedByIdOrderByCreatedAtDesc(Long userId);
}