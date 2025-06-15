package com.example.banking.service;

import com.example.banking.domain.AccountTransaction;
import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * The type Transaction service.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    /**
     * Gets account transactions.
     *
     * @param accountId the account id
     * @param pageable  the pageable
     * @return the account transactions
     */
    public Page<AccountTransaction> getAccountTransactions(UUID accountId, Pageable pageable) {
        // Validate account exists
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }

        return transactionRepository.findTransactionsByAccountId(accountId, pageable);
    }
}