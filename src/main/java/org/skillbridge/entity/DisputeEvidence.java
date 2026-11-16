package org.skillbridge.entity;

import jakarta.persistence.*;
import org.skillbridge.enums.DisputeEvidenceType;

import java.time.Instant;

@Entity
@Table(name = "dispute_evidence")
public class DisputeEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispute_id", nullable = false)
    private Dispute dispute;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_type", nullable = false, length = 30)
    private DisputeEvidenceType evidenceType;

    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Dispute getDispute() { return dispute; }
    public void setDispute(Dispute dispute) { this.dispute = dispute; }

    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }

    public DisputeEvidenceType getEvidenceType() { return evidenceType; }
    public void setEvidenceType(DisputeEvidenceType evidenceType) { this.evidenceType = evidenceType; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() { return createdAt; }
}
