package org.skillbridge.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AssignmentCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        String instructions,
        @NotNull @Min(1) @Max(1000) Integer maxScore,
        @DecimalMin(value = "0.0", inclusive = true) @DecimalMax(value = "100.0", inclusive = true) BigDecimal weight,
        LocalDate dueDate,
        Boolean published
) {}