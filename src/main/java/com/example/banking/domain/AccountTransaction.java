package com.example.banking.domain;

import com.example.banking.util.CurrencyValidator;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountTransaction {

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer = 15, fraction = 2)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(nullable = false, length = 3)
    private String currency;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TransactionType type;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private String description;

    // Reference for transfers (links IN/OUT transactions)
    @Column(name = "correlation_id")
    private UUID correlationId;

    // Validate currency format
    @PrePersist
    @PreUpdate
    private void validateCurrency() {
        CurrencyValidator.validateCurrencyCode(this.currency);
    }
}