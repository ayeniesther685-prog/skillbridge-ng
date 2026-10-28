package org.skillbridge.repository;

import org.skillbridge.entity.Contract;
import org.skillbridge.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    Optional<Contract> findByJobId(Long jobId);

    boolean existsByJobId(Long jobId);

    Page<Contract> findByClientId(Long clientId, Pageable pageable);

    Page<Contract> findByFreelancerId(Long freelancerId, Pageable pageable);

    long countByStatus(ContractStatus status);
}