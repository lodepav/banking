package com.example.banking.dto;

import com.example.banking.util.ValidCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The type Transfer request.
 */
@Schema(description = "Funds transfer request")
public record TransferRequest(
        @NotNull
        @Schema(description = "Sender account ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID fromAccountId,

        @NotNull
        @Schema(description = "Receiver account ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID toAccountId,

        @NotNull
        @Positive
        @Digits(integer = 15, fraction = 2)
        @Schema(description = "Transfer amount", example = "150.75")
        BigDecimal amount,

        @NotBlank
        @Size(min = 3, max = 3)
        @Schema(description = "Currency of the transfer amount", example = "USD")
        @ValidCurrency
        String currency
) {}