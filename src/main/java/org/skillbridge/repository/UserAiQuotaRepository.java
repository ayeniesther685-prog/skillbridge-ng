package org.skillbridge.repository;

import org.skillbridge.entity.UserAiQuota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAiQuotaRepository extends JpaRepository<UserAiQuota, Long> {
    Optional<UserAiQuota> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    long countByTierId(Long tierId);
}