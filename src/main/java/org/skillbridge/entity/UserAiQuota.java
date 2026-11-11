package org.skillbridge.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "user_ai_quotas")
public class UserAiQuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private AiQuotaTier tier;

    @Column(name = "bonus_credits", nullable = false)
    private Integer bonusCredits = 0;

    @Column(name = "used_today", nullable = false)
    private Integer usedToday = 0;

    @Column(name = "used_this_month", nullable = false)
    private Integer usedThisMonth = 0;

    @Column(name = "daily_reset_at", nullable = false)
    private Instant dailyResetAt;

    @Column(name = "monthly_reset_at", nullable = false)
    private Instant monthlyResetAt;

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
    void onUpdate() { this.updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public AiQuotaTier getTier() { return tier; }
    public void setTier(AiQuotaTier tier) { this.tier = tier; }
    public Integer getBonusCredits() { return bonusCredits; }
    public void setBonusCredits(Integer bonusCredits) { this.bonusCredits = bonusCredits; }
    public Integer getUsedToday() { return usedToday; }
    public void setUsedToday(Integer usedToday) { this.usedToday = usedToday; }
    public Integer getUsedThisMonth() { return usedThisMonth; }
    public void setUsedThisMonth(Integer usedThisMonth) { this.usedThisMonth = usedThisMonth; }
    public Instant getDailyResetAt() { return dailyResetAt; }
    public void setDailyResetAt(Instant dailyResetAt) { this.dailyResetAt = dailyResetAt; }
    public Instant getMonthlyResetAt() { return monthlyResetAt; }
    public void setMonthlyResetAt(Instant monthlyResetAt) { this.monthlyResetAt = monthlyResetAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}