package org.skillbridge.repository;

import org.skillbridge.entity.Job;
import org.skillbridge.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    Page<Job> findByClientId(Long clientId, Pageable pageable);

    long countByStatus(JobStatus status);

    long countByClientId(Long clientId);
}