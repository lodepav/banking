package com.example.banking.service;

import com.example.banking.domain.Account;
import com.example.banking.exception.ClientNotFoundException;
import com.example.banking.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The type Account service.
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    /**
     * Gets client accounts.
     *
     * @param clientId the client id
     * @return the client accounts
     */
    public List<Account> getClientAccounts(String clientId) {
        List<Account> accounts = accountRepository.findByClientId(clientId);
        if (accounts.isEmpty()) {
            throw new ClientNotFoundException(clientId);
        }
        return accounts;
    }
}