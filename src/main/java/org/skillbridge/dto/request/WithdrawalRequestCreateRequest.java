package org.skillbridge.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WithdrawalRequestCreateRequest(
        @NotNull @Min(1) Integer amountPoints,
        @NotBlank @Size(max = 100) String bankName,
        @NotBlank @Size(max = 30) String accountNumber,
        @NotBlank @Size(max = 200) String accountName
) {}