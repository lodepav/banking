package com.example.banking.domain;

import com.example.banking.exception.InsufficientFundsException;
import com.example.banking.util.ValidCurrency;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(nullable = false, length = 3)
    @ValidCurrency
    private String currency;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 15, fraction = 2)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    // Business logic method for safe balance updates
    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        BigDecimal newBalance = this.balance.subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException(
                    "Account " + id + " has insufficient funds. " +
                            "Current balance: " + balance + " " + currency + ", " +
                            "attempted debit: " + amount + " " + currency
            );
        }
        this.balance = newBalance;
    }
}