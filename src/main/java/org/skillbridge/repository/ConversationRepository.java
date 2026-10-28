package org.skillbridge.repository;

import org.skillbridge.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE c.participantA.id = :userId OR c.participantB.id = :userId")
    Page<Conversation> findAllForUser(@Param("userId") Long userId, Pageable pageable);

    Optional<Conversation> findByParticipantAIdAndParticipantBId(Long a, Long b);
}