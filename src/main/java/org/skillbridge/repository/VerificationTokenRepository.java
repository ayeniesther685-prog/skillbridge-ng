package org.skillbridge.repository;

import org.skillbridge.entity.VerificationToken;
import org.skillbridge.enums.VerificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByTokenHash(String tokenHash);

    List<VerificationToken> findByUserIdAndChannelAndConsumedAtIsNull(Long userId, VerificationChannel channel);

    /**
     * Invalidate every outstanding EMAIL token for a user.
     * Called before issuing a new token so only the newest link works.
     */
    @Modifying
    @Query("""
            UPDATE VerificationToken t
               SET t.consumedAt = :now
             WHERE t.user.id = :userId
               AND t.channel = :channel
               AND t.consumedAt IS NULL
            """)
    int invalidateAllForUser(@Param("userId") Long userId,
                             @Param("channel") VerificationChannel channel,
                             @Param("now") Instant now);

    /** Cleanup job can use this to purge expired rows periodically. */
    @Modifying
    @Query("DELETE FROM VerificationToken t WHERE t.expiresAt < :cutoff")
    int deleteAllExpiredBefore(@Param("cutoff") Instant cutoff);
}
