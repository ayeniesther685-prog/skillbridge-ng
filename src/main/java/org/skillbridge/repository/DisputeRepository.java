package org.skillbridge.repository;

import org.skillbridge.entity.Dispute;
import org.skillbridge.enums.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    List<Dispute> findByContractIdOrderByCreatedAtDesc(Long contractId);

    List<Dispute> findByMilestoneIdOrderByCreatedAtDesc(Long milestoneId);

    Page<Dispute> findByStatus(DisputeStatus status, Pageable pageable);

    Page<Dispute> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * True if any dispute on this contract is in a status that blocks payment release.
     * (OPEN, UNDER_REVIEW, ESCALATED)
     */
    @Query("""
            SELECT COUNT(d) > 0 FROM Dispute d
             WHERE d.contract.id = :contractId
               AND d.status IN ('OPEN', 'UNDER_REVIEW', 'ESCALATED')
            """)
    boolean existsBlockingForContract(@Param("contractId") Long contractId);

    /** Open dispute for a contract (at most one, enforced by DB unique index). */
    @Query("""
            SELECT d FROM Dispute d
             WHERE d.contract.id = :contractId
               AND d.status = 'OPEN'
            """)
    Optional<Dispute> findOpenForContract(@Param("contractId") Long contractId);

    /** Open dispute for a specific milestone (at most one). */
    @Query("""
            SELECT d FROM Dispute d
             WHERE d.milestone.id = :milestoneId
               AND d.status = 'OPEN'
            """)
    Optional<Dispute> findOpenForMilestone(@Param("milestoneId") Long milestoneId);
}
