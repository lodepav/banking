package com.example.banking.service;

import com.example.banking.domain.AccountTransaction;
import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public List<AccountTransaction> getAccountTransactions(UUID accountId, Pageable pageable) {
        // Validate account exists
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }

        return transactionRepository.findTransactionsByAccountId(accountId, pageable);
    }
}