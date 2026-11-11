package org.skillbridge.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "daily_challenges")
public class DailyChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "point_reward", nullable = false)
    private Integer pointReward;

    @Column(name = "difficulty", nullable = false, length = 20)
    private String difficulty = "BEGINNER";

    @Column(name = "max_winners")
    private Integer maxWinners;

    @Column(name = "winners_count", nullable = false)
    private Integer winnersCount = 0;

    @Column(name = "active_from", nullable = false)
    private Instant activeFrom;

    @Column(name = "active_until", nullable = false)
    private Instant activeUntil;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.activeFrom == null) this.activeFrom = now;
    }

    @PreUpdate
    void onUpdate() { this.updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getPointReward() { return pointReward; }
    public void setPointReward(Integer pointReward) { this.pointReward = pointReward; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public Integer getMaxWinners() { return maxWinners; }
    public void setMaxWinners(Integer maxWinners) { this.maxWinners = maxWinners; }
    public Integer getWinnersCount() { return winnersCount; }
    public void setWinnersCount(Integer winnersCount) { this.winnersCount = winnersCount; }
    public Instant getActiveFrom() { return activeFrom; }
    public void setActiveFrom(Instant activeFrom) { this.activeFrom = activeFrom; }
    public Instant getActiveUntil() { return activeUntil; }
    public void setActiveUntil(Instant activeUntil) { this.activeUntil = activeUntil; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}