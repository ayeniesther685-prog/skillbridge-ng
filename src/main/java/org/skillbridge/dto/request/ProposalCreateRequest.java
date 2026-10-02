package org.skillbridge.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProposalCreateRequest(
        @NotBlank @Size(max = 4000) String coverLetter,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal proposedPrice,
        @Positive Integer estimatedDeliveryDays
) {}