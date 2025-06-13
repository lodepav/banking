package com.example.banking.domain

import com.example.banking.exception.InsufficientFundsException
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class AccountSpec extends Specification {

    def "credit should increase balance by given amount"() {
        given:
        def acc = Account.builder()
                .id(UUID.randomUUID())
                .clientId("c1")
                .currency("USD")
                .balance(new BigDecimal("100.00"))
                .build()

        when:
        acc.credit(new BigDecimal("25.50"))

        then:
        acc.balance == new BigDecimal("125.50")
    }

    def "debit should decrease balance when funds are sufficient"() {
        given:
        def acc = Account.builder()
                .id(UUID.randomUUID())
                .clientId("c2")
                .currency("EUR")
                .balance(new BigDecimal("200.00"))
                .build()

        when:
        acc.debit(new BigDecimal("50.00"))

        then:
        acc.balance == new BigDecimal("150.00")
    }

    def "debit exact balance sets balance to zero"() {
        given:
        def acc = Account.builder()
                .id(UUID.randomUUID())
                .clientId("c3")
                .currency("GBP")
                .balance(new BigDecimal("75.00"))
                .build()

        when:
        acc.debit(new BigDecimal("75.00"))

        then:
        acc.balance == BigDecimal.ZERO
    }

    def "debit should throw when funds are insufficient"() {
        given:
        def uuid = UUID.randomUUID()
        def acc = Account.builder()
                .id(uuid)
                .clientId("c4")
                .currency("JPY")
                .balance(new BigDecimal("10.00"))
                .build()

        when:
        acc.debit(new BigDecimal("10.01"))

        then:
        def ex = thrown(InsufficientFundsException)
        ex.message.contains("Account $uuid")
        ex.message.contains("Current balance: 10.00 JPY")
        ex.message.contains("attempted debit: 10.01 JPY")
    }
}
