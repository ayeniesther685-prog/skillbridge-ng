package org.skillbridge.entity;

import jakarta.persistence.*;
import org.skillbridge.enums.RevisionStatus;

import java.time.Instant;

@Entity
@Table(name = "contract_revisions")
public class ContractRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "milestone_id", nullable = false)
    private Milestone milestone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @Column(name = "reason", nullable = false, length = 2000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RevisionStatus status = RevisionStatus.OPEN;

    @Column(name = "is_new_requirements", nullable = false)
    private boolean isNewRequirements = false;

    @Column(name = "freelancer_response", length = 2000)
    private String freelancerResponse;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Milestone getMilestone() { return milestone; }
    public void setMilestone(Milestone milestone) { this.milestone = milestone; }

    public User getRequestedBy() { return requestedBy; }
    public void setRequestedBy(User requestedBy) { this.requestedBy = requestedBy; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public RevisionStatus getStatus() { return status; }
    public void setStatus(RevisionStatus status) { this.status = status; }

    public boolean isNewRequirements() { return isNewRequirements; }
    public void setNewRequirements(boolean newRequirements) { this.isNewRequirements = newRequirements; }

    public String getFreelancerResponse() { return freelancerResponse; }
    public void setFreelancerResponse(String freelancerResponse) { this.freelancerResponse = freelancerResponse; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
