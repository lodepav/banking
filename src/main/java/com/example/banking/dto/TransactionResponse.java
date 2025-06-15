package com.example.banking.dto;

import com.example.banking.domain.AccountTransaction;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Transaction record")
public record TransactionResponse(
        @Schema(description = "Transaction ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Transaction amount (negative for debits)", example = "-100.00")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal amount,

        @Schema(description = "Transaction currency", example = "USD")
        String currency,

        @Schema(description = "Transaction type", example = "TRANSFER_OUT")
        String type,

        @Schema(description = "Transaction timestamp", example = "2023-07-15T11:45:30Z")
        String createdAt,

        @Schema(description = "Transaction description", example = "Transfer to account 123e4567")
        String description,

        @Schema(description = "Correlation ID for linked transactions", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID correlationId
) {
    public static TransactionResponse fromDomain(AccountTransaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getType().name(),
                transaction.getCreatedAt().toString(),
                transaction.getDescription(),
                transaction.getCorrelationId()
        );
    }
}
