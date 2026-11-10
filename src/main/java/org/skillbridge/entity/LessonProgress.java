package org.skillbridge.entity;

import jakarta.persistence.*;
import org.skillbridge.enums.LessonProgressStatus;

import java.time.Instant;

@Entity
@Table(name = "lesson_progress")
public class LessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_record_id", nullable = false)
    private StudentRecord studentRecord;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private CourseLesson lesson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private LessonProgressStatus status = LessonProgressStatus.NOT_STARTED;

    @Column(name = "completed_at")
    private Instant completedAt;

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
    public StudentRecord getStudentRecord() { return studentRecord; }
    public void setStudentRecord(StudentRecord studentRecord) { this.studentRecord = studentRecord; }
    public CourseLesson getLesson() { return lesson; }
    public void setLesson(CourseLesson lesson) { this.lesson = lesson; }
    public LessonProgressStatus getStatus() { return status; }
    public void setStatus(LessonProgressStatus status) { this.status = status; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}