package org.skillbridge.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_a", nullable = false)
    private User participantA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_b", nullable = false)
    private User participantB;

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getParticipantA() { return participantA; }
    public void setParticipantA(User participantA) { this.participantA = participantA; }

    public User getParticipantB() { return participantB; }
    public void setParticipantB(User participantB) { this.participantB = participantB; }

    public Instant getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(Instant lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public Instant getCreatedAt() { return createdAt; }

    /** Returns the other participant given one of them. */
    public User otherParticipant(User me) {
        return me.getId().equals(participantA.getId()) ? participantB : participantA;
    }
}