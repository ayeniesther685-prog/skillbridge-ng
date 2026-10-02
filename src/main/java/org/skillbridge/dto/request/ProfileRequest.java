package org.skillbridge.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProfileRequest(
        @Size(max = 200) String headline,
        @Size(max = 300) String website,
        @Size(max = 300) String linkedinUrl,
        @Size(max = 300) String githubUrl,
        @Size(max = 300) String twitterUrl,
        @Size(max = 50) String availability,
        @DecimalMin(value = "0.0", inclusive = true) BigDecimal hourlyRate
) {}