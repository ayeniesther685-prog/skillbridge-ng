package org.skillbridge.dto.request;

import jakarta.validation.constraints.*;
import org.skillbridge.enums.ExperienceLevel;
import org.skillbridge.enums.JobType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record JobUpdateRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 4000) String description,
        @DecimalMin(value = "0.0", inclusive = true) BigDecimal budgetMin,
        @DecimalMin(value = "0.0", inclusive = true) BigDecimal budgetMax,
        @Size(max = 150) String location,
        @NotNull JobType jobType,
        @NotNull ExperienceLevel experienceLevel,
        LocalDate deadline,
        Set<Long> skillIds
) {}