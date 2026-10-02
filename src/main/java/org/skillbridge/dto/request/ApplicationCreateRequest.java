package org.skillbridge.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ApplicationCreateRequest(
        @DecimalMin(value = "0.0", inclusive = true) BigDecimal expectedBudget,
        @NotNull @Valid ProposalCreateRequest proposal
) {}