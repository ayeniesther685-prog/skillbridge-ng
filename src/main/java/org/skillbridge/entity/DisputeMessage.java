package org.skillbridge.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "dispute_messages")
public class DisputeMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispute_id", nullable = false)
    private Dispute dispute;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "message", nullable = false, length = 4000)
    private String message;

    @Column(name = "is_from_admin", nullable = false)
    private boolean isFromAdmin = false;

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

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isFromAdmin() { return isFromAdmin; }
    public void setFromAdmin(boolean fromAdmin) { this.isFromAdmin = fromAdmin; }

    public Instant getCreatedAt() { return createdAt; }
}
