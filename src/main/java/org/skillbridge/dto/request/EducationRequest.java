package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EducationRequest(
        @NotBlank @Size(max = 200) String institution,
        @Size(max = 150) String degree,
        @Size(max = 150) String fieldOfStudy,
        LocalDate startDate,
        LocalDate endDate,
        @Size(max = 1000) String description
) {}