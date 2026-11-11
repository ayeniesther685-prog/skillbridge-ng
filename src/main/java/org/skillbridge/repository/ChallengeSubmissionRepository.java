package org.skillbridge.repository;

import org.skillbridge.entity.ChallengeSubmission;
import org.skillbridge.enums.ChallengeSubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeSubmissionRepository extends JpaRepository<ChallengeSubmission, Long> {
    Optional<ChallengeSubmission> findByChallengeIdAndUserId(Long challengeId, Long userId);
    boolean existsByChallengeIdAndUserId(Long challengeId, Long userId);
    List<ChallengeSubmission> findByChallengeIdOrderByCreatedAtDesc(Long challengeId);
    List<ChallengeSubmission> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByChallengeIdAndStatus(Long challengeId, ChallengeSubmissionStatus status);
}