package org.skillbridge.entity;

import jakarta.persistence.*;
import org.skillbridge.enums.EnrollmentStatus;

import java.time.Instant;

@Entity
@Table(name = "course_enrollments")
public class CourseEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_record_id", nullable = false)
    private StudentRecord studentRecord;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private AcademicSemester semester;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EnrollmentStatus status = EnrollmentStatus.ENROLLED;

    @Column(name = "final_grade", length = 5)
    private String finalGrade;

    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private Instant enrolledAt;

    @PrePersist
    void onCreate() { this.enrolledAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public StudentRecord getStudentRecord() { return studentRecord; }
    public void setStudentRecord(StudentRecord studentRecord) { this.studentRecord = studentRecord; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public AcademicSemester getSemester() { return semester; }
    public void setSemester(AcademicSemester semester) { this.semester = semester; }
    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }
    public String getFinalGrade() { return finalGrade; }
    public void setFinalGrade(String finalGrade) { this.finalGrade = finalGrade; }
    public Instant getEnrolledAt() { return enrolledAt; }
}