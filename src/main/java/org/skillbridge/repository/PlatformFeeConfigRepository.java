package org.skillbridge.repository;

import org.skillbridge.entity.PlatformFeeConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformFeeConfigRepository extends JpaRepository<PlatformFeeConfig, Long> {
    Optional<PlatformFeeConfig> findByRoleNameIgnoreCaseAndActiveTrue(String roleName);
}