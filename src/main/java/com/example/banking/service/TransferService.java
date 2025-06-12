package com.example.banking.service;

import com.example.banking.domain.*;
import com.example.banking.dto.TransferRequest;
import com.example.banking.dto.TransferResult;
import com.example.banking.exception.*;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;

    @Transactional
    public TransferResult transferFunds(TransferRequest request) {
        // Prevent same account transfer
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new SameAccountTransferException("Cannot transfer to the same account");
        }

        // Load accounts in ID order to prevent deadlocks
        List<UUID> accountIds = Arrays.asList(
                request.fromAccountId(),
                request.toAccountId()
        );
        Collections.sort(accountIds);

        Account sender = accountRepository.findByIdWithLock(accountIds.get(0))
                .orElseThrow(() -> new AccountNotFoundException(accountIds.getFirst()));

        Account receiver = accountRepository.findByIdWithLock(accountIds.get(1))
                .orElseThrow(() -> new AccountNotFoundException(accountIds.get(1)));

        // Reassign sender/receiver based on request
        if (accountIds.getFirst().equals(request.toAccountId())) {
            Account temp = sender;
            sender = receiver;
            receiver = temp;
        }

        // Validate receiver currency matches transfer currency
        if (!receiver.getCurrency().equals(request.currency())) {
            throw new CurrencyMismatchException(
                    "Receiver account requires " + receiver.getCurrency() +
                            ", but transfer requested in " + request.currency()
            );
        }

        // Convert amount if currencies differ
        BigDecimal exchangeRate = BigDecimal.ONE;
        BigDecimal amountToDebit = request.amount();

        if (!sender.getCurrency().equals(request.currency())) {
            exchangeRate = exchangeRateService.getExchangeRate(
                    request.currency(),
                    sender.getCurrency()
            );
            amountToDebit = request.amount().multiply(exchangeRate)
                    .setScale(2, RoundingMode.HALF_EVEN);
        }

        sender.debit(amountToDebit);
        receiver.credit(request.amount());

        // Save account updates
        accountRepository.save(sender);
        accountRepository.save(receiver);

        // Record transactions and get correlation ID
        UUID correlationId = recordTransactions(
                sender,
                receiver,
                amountToDebit,
                request.amount(),
                exchangeRate
        );

        // Return transfer result with all necessary details
        return new TransferResult(
                correlationId,
                amountToDebit,
                sender.getCurrency(),
                request.amount(),
                receiver.getCurrency(),
                exchangeRate,
                sender.getBalance(),  // New balance after transfer
                receiver.getBalance()  // New balance after transfer
        );
    }

    private UUID recordTransactions(
            Account sender,
            Account receiver,
            BigDecimal debitAmount,
            BigDecimal creditAmount,
            BigDecimal exchangeRate
    ) {
        UUID correlationId = UUID.randomUUID();
        Instant now = Instant.now();

        // Build description with exchange rate info
        String description = "Transfer to " + receiver.getId();
        if (!exchangeRate.equals(BigDecimal.ONE)) {
            description += String.format(" (Rate: %s %s/%s)",
                    exchangeRate.stripTrailingZeros().toPlainString(),
                    sender.getCurrency(),
                    receiver.getCurrency());
        }

        // Sender transaction (outflow)
        AccountTransaction debitTransaction = AccountTransaction.builder()
                .account(sender)
                .amount(debitAmount.negate())
                .currency(sender.getCurrency())
                .type(AccountTransaction.TransactionType.TRANSFER_OUT)
                .correlationId(correlationId)
                .createdAt(now)
                .description(description)
                .build();

        // Receiver transaction (inflow)
        AccountTransaction creditTransaction = AccountTransaction.builder()
                .account(receiver)
                .amount(creditAmount)
                .currency(receiver.getCurrency())
                .type(AccountTransaction.TransactionType.TRANSFER_IN)
                .correlationId(correlationId)
                .createdAt(now)
                .description("Transfer from " + sender.getId())
                .build();

        transactionRepository.saveAll(List.of(debitTransaction, creditTransaction));

        return correlationId;
    }
}