package org.skillbridge.entity;

import jakarta.persistence.*;
import org.skillbridge.enums.VerificationEventType;
import org.skillbridge.enums.VerificationStatus;

import java.time.Instant;

@Entity
@Table(name = "provider_verification_events")
public class ProviderVerificationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "verification_id", nullable = false)
    private ProviderVerification verification;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private VerificationEventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private VerificationStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", length = 30)
    private VerificationStatus toStatus;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProviderVerification getVerification() { return verification; }
    public void setVerification(ProviderVerification verification) { this.verification = verification; }

    public VerificationEventType getEventType() { return eventType; }
    public void setEventType(VerificationEventType eventType) { this.eventType = eventType; }

    public User getActor() { return actor; }
    public void setActor(User actor) { this.actor = actor; }

    public VerificationStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(VerificationStatus fromStatus) { this.fromStatus = fromStatus; }

    public VerificationStatus getToStatus() { return toStatus; }
    public void setToStatus(VerificationStatus toStatus) { this.toStatus = toStatus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getCreatedAt() { return createdAt; }
}