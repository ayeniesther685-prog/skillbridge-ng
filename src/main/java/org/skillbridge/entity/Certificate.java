package org.skillbridge.entity;

import jakarta.persistence.*;
import org.skillbridge.enums.CertificateStatus;

import java.time.Instant;

@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_record_id", nullable = false)
    private StudentRecord studentRecord;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "certificate_number", nullable = false, unique = true, length = 50)
    private String certificateNumber;

    @Column(name = "final_score", nullable = false)
    private Integer finalScore = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CertificateStatus status = CertificateStatus.ISSUED;

    @Column(name = "verification_hash", nullable = false, length = 128)
    private String verificationHash;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.issuedAt = now;
        this.createdAt = now;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public StudentRecord getStudentRecord() { return studentRecord; }
    public void setStudentRecord(StudentRecord studentRecord) { this.studentRecord = studentRecord; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public String getCertificateNumber() { return certificateNumber; }
    public void setCertificateNumber(String certificateNumber) { this.certificateNumber = certificateNumber; }
    public Integer getFinalScore() { return finalScore; }
    public void setFinalScore(Integer finalScore) { this.finalScore = finalScore; }
    public CertificateStatus getStatus() { return status; }
    public void setStatus(CertificateStatus status) { this.status = status; }
    public String getVerificationHash() { return verificationHash; }
    public void setVerificationHash(String verificationHash) { this.verificationHash = verificationHash; }
    public Instant getIssuedAt() { return issuedAt; }
    public Instant getCreatedAt() { return createdAt; }
}