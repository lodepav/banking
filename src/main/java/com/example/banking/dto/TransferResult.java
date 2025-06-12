package com.example.banking.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferResult(
        UUID correlationId,
        BigDecimal debitedAmount,
        String senderCurrency,
        BigDecimal creditedAmount,
        String receiverCurrency,
        BigDecimal exchangeRate,
        BigDecimal senderNewBalance,
        BigDecimal receiverNewBalance
) {}
