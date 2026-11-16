package org.skillbridge.repository;

import org.skillbridge.entity.ContractRevision;
import org.skillbridge.enums.RevisionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContractRevisionRepository extends JpaRepository<ContractRevision, Long> {

    List<ContractRevision> findByMilestoneIdOrderByCreatedAtDesc(Long milestoneId);

    List<ContractRevision> findByMilestoneContractIdOrderByCreatedAtDesc(Long contractId);

    long countByMilestoneId(Long milestoneId);

    long countByMilestoneIdAndStatus(Long milestoneId, RevisionStatus status);

    @Query("""
            SELECT r FROM ContractRevision r
            WHERE r.milestone.id = :milestoneId
              AND r.status = 'OPEN'
            """)
    List<ContractRevision> findOpenByMilestone(@Param("milestoneId") Long milestoneId);
}
