package org.skillbridge.dto.response;

import org.skillbridge.enums.VerificationStatus;

public record ProviderVerificationStatusResponse(
        Long userId,
        VerificationStatus status,
        boolean verified
) {}
