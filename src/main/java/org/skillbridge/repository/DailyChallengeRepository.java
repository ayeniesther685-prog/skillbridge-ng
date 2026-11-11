package org.skillbridge.repository;

import org.skillbridge.entity.DailyChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface DailyChallengeRepository extends JpaRepository<DailyChallenge, Long> {
    List<DailyChallenge> findByActiveTrueAndActiveUntilAfterOrderByCreatedAtDesc(Instant now);
    List<DailyChallenge> findAllByOrderByCreatedAtDesc();
}