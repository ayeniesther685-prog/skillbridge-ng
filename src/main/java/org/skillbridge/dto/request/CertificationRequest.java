package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CertificationRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 200) String issuingOrganization,
        LocalDate issueDate,
        LocalDate expiryDate,
        @Size(max = 400) String credentialUrl
) {}