package com.example.banking.dto;

import com.example.banking.domain.Account;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The type Account response.
 */
@Schema(description = "Account information")
public record AccountResponse(
        @Schema(description = "Account ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Account currency (ISO 4217)", example = "USD")
        String currency,

        @Schema(description = "Current balance", example = "1500.75")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal balance,

        @Schema(description = "Account creation timestamp", example = "2023-07-15T10:30:00Z")
        String createdAt
) {
    /**
     * From domain account response.
     *
     * @param account the account
     * @return the account response
     */
    public static AccountResponse fromDomain(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getCurrency(),
                account.getBalance(),
                account.getCreatedAt().toString()
        );
    }
}