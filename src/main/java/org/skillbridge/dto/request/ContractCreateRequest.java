package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotNull;

public record ContractCreateRequest(
        @NotNull Long applicationId
) {}