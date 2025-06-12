package com.example.banking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferResponse(
        @Schema(description = "Unique correlation ID for the transfer")
        UUID correlationId,

        @Schema(description = "Debited amount from sender")
        BigDecimal debitedAmount,

        @Schema(description = "Sender account currency")
        String senderCurrency,

        @Schema(description = "Credited amount to receiver")
        BigDecimal creditedAmount,

        @Schema(description = "Receiver account currency")
        String receiverCurrency,

        @Schema(description = "Exchange rate used (if applicable)")
        BigDecimal exchangeRate,

        @Schema(description = "New sender account balance")
        BigDecimal senderNewBalance,

        @Schema(description = "New receiver account balance")
        BigDecimal receiverNewBalance
) {
    public static TransferResponse fromTransferResult(TransferResult result) {
        return new TransferResponse(
                result.correlationId(),
                result.debitedAmount(),
                result.senderCurrency(),
                result.creditedAmount(),
                result.receiverCurrency(),
                result.exchangeRate(),
                result.senderNewBalance(),
                result.receiverNewBalance()
        );
    }
}
