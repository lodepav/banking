package com.example.banking.repository;

import com.example.banking.domain.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface Account repository.
 */
public interface AccountRepository extends JpaRepository<Account, UUID> {
    /**
     * Find by id with lock optional.
     *
     * @param id the id
     * @return the optional
     */
// Lock account for update
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdWithLock(@Param("id") UUID id);

    /**
     * Find by client id list.
     *
     * @param clientId the client id
     * @return the list
     */
    List<Account> findByClientId(String clientId);
}
