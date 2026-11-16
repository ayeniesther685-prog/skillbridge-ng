package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DisputeEvidenceCreateRequest(
        @NotBlank @Size(max = 30) String evidenceType,
        @NotBlank @Size(max = 1000) String url,
        @Size(max = 1000) String description
) {}
