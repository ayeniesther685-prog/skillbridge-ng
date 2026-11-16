package org.skillbridge.dto.request;

import jakarta.validation.constraints.*;
import org.skillbridge.enums.ProviderType;

public record ProviderVerificationSubmitRequest(
        @NotNull ProviderType providerType,
        @NotBlank @Size(max = 200) String legalName,
        @Size(max = 50) String cacRegistrationNumber,
        @NotBlank @Email @Size(max = 180) String officialEmail,
        @NotBlank @Size(max = 30) String officialPhone,
        @NotBlank @Size(max = 500) String physicalAddress,
        @Size(max = 300) String website,
        @Size(max = 300) String linkedinUrl,
        @NotBlank @Size(max = 200) String authorizedRepName,
        @NotBlank @Size(max = 100) String authorizedRepRole
) {}