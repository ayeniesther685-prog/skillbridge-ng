package org.skillbridge.dto.response;

import org.skillbridge.enums.ExperienceLevel;
import org.skillbridge.enums.JobStatus;
import org.skillbridge.enums.JobType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record JobResponse(
        Long id,
        Long clientId,
        String clientName,
        String title,
        String description,
        BigDecimal budgetMin,
        BigDecimal budgetMax,
        String location,
        JobType jobType,
        ExperienceLevel experienceLevel,
        JobStatus status,
        LocalDate deadline,
        List<SkillResponse> requiredSkills,
        Instant createdAt,
        Instant updatedAt
) {}