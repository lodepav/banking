package com.example.banking.service;

import com.example.banking.domain.*;
import com.example.banking.dto.TransferRequest;
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
    public void transferFunds(TransferRequest request) {
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
                .orElseThrow(() -> new AccountNotFoundException(accountIds.get(0)));

        Account receiver = accountRepository.findByIdWithLock(accountIds.get(1))
                .orElseThrow(() -> new AccountNotFoundException(accountIds.get(1)));

        // Reassign sender/receiver based on request
        if (accountIds.get(0).equals(request.toAccountId())) {
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
        BigDecimal amountToDebit = request.amount();
        if (!sender.getCurrency().equals(request.currency())) {
            BigDecimal rate = exchangeRateService.getExchangeRate(
                    request.currency(),
                    sender.getCurrency()
            );
            amountToDebit = request.amount().multiply(rate)
                    .setScale(2, RoundingMode.HALF_EVEN);
        }

        // Perform balance updates
        sender.debit(amountToDebit);
        receiver.credit(request.amount());

        // Save account updates
        accountRepository.save(sender);
        accountRepository.save(receiver);

        // Record transactions
        recordTransactions(sender, receiver, amountToDebit, request.amount());
    }

    private void recordTransactions(
            Account sender,
            Account receiver,
            BigDecimal debitAmount,
            BigDecimal creditAmount
    ) {
        UUID correlationId = UUID.randomUUID();
        Instant now = Instant.now();

        // Sender transaction (outflow)
        AccountTransaction debitTransaction = AccountTransaction.builder()
                .account(sender)
                .amount(debitAmount.negate())
                .currency(sender.getCurrency())
                .type(AccountTransaction.TransactionType.TRANSFER_OUT)
                .correlationId(correlationId)
                .createdAt(now)
                .description("Transfer to " + receiver.getId())
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
    }
}