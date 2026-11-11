package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChallengeSubmitRequest(
        @NotBlank @Size(max = 20000) String content
) {}