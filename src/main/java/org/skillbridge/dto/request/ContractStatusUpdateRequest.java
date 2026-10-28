package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotNull;
import org.skillbridge.enums.ContractStatus;

public record ContractStatusUpdateRequest(
        @NotNull ContractStatus status
) {}