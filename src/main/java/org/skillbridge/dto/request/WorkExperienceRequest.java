package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record WorkExperienceRequest(
        @NotBlank @Size(max = 200) String company,
        @NotBlank @Size(max = 150) String position,
        @Size(max = 1000) String description,
        LocalDate startDate,
        LocalDate endDate,
        Boolean current
) {}