package com.example.banking.repository;

import com.example.banking.domain.AccountTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<AccountTransaction, UUID> {
    @Query("SELECT t FROM AccountTransaction t WHERE t.account.id = :accountId " +
            "ORDER BY t.createdAt DESC, t.id DESC")
    List<AccountTransaction> findTransactionsByAccountId(
            @Param("accountId") UUID accountId,
            Pageable pageable
    );
}
