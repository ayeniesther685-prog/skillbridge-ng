package org.skillbridge.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "academic_departments")
public class AcademicDepartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private AcademicProgram program;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() { this.createdAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AcademicProgram getProgram() { return program; }
    public void setProgram(AcademicProgram program) { this.program = program; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getCreatedAt() { return createdAt; }
}