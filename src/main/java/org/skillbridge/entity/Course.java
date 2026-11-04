package org.skillbridge.entity;

import jakarta.persistence.*;
import org.skillbridge.enums.AcademicLevel;

import java.time.Instant;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private AcademicDepartment department;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 20)
    private AcademicLevel level = AcademicLevel.BEGINNER;

    @Column(name = "credits", nullable = false)
    private int credits = 3;

    @Column(name = "duration_hours")
    private Integer durationHours;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

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
    public AcademicDepartment getDepartment() { return department; }
    public void setDepartment(AcademicDepartment department) { this.department = department; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public AcademicLevel getLevel() { return level; }
    public void setLevel(AcademicLevel level) { this.level = level; }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    public Integer getDurationHours() { return durationHours; }
    public void setDurationHours(Integer durationHours) { this.durationHours = durationHours; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}