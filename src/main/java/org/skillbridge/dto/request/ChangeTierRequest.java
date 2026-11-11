package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeTierRequest(
        @NotBlank @Size(max = 30) String tierName
) {}