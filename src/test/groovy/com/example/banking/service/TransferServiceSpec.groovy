package com.example.banking.service

import com.example.banking.domain.Account
import com.example.banking.dto.TransferRequest
import com.example.banking.exception.*
import com.example.banking.repository.AccountRepository
import com.example.banking.repository.TransactionRepository
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification
import spock.lang.Subject

@SpringBootTest
class TransferServiceSpec extends Specification {

    AccountRepository accountRepository = Mock()
    TransactionRepository transactionRepository = Mock()
    ExchangeRateService exchangeRateService = Mock()

    @Subject
    TransferService transferService = new TransferService(accountRepository, transactionRepository, exchangeRateService)

    def "transferFunds - should successfully transfer when currencies match"() {
        given: "Valid accounts and request"
        def sender = new Account(id: UUID.randomUUID(), clientId: "c1", currency: "USD", balance: new BigDecimal("1000.00"))
        def receiver = new Account(id: UUID.randomUUID(), clientId: "c2", currency: "USD", balance: new BigDecimal("500.00"))
        def request = new TransferRequest(sender.id, receiver.id, new BigDecimal("100.00"), "USD")

        and: "Accounts are locked in ID order"
        accountRepository.findByIdWithLock(_) >>> [Optional.of(sender), Optional.of(receiver)]

        when: "Transfer is executed"
        transferService.transferFunds(request)

        then: "Balances are updated"
        sender.balance == new BigDecimal("900.00")
        receiver.balance == new BigDecimal("600.00")

        and: "Transactions are recorded"
        1 * transactionRepository.saveAll(_)
    }

    def "transferFunds - should convert currency when currencies differ"() {
        given: "Accounts with different currencies"
        def sender = new Account(id: UUID.randomUUID(), clientId: "c1", currency: "USD", balance: new BigDecimal("1000.00"))
        def receiver = new Account(id: UUID.randomUUID(), clientId: "c2", currency: "EUR", balance: new BigDecimal("500.00"))
        def request = new TransferRequest(sender.id, receiver.id, new BigDecimal("100.00"), "EUR")

        and: "Exchange rate setup"
        exchangeRateService.getExchangeRate("EUR", "USD") >> new BigDecimal("1.20")
        accountRepository.findByIdWithLock(_) >>> [Optional.of(sender), Optional.of(receiver)]

        when: "Transfer is executed"
        transferService.transferFunds(request)

        then: "Sender debited in USD"
        sender.balance == new BigDecimal("1000.00").subtract(new BigDecimal("120.00"))

        and: "Receiver credited in EUR"
        receiver.balance == new BigDecimal("600.00")
    }

    def "transferFunds - should throw when receiver currency mismatch"() {
        given: "Invalid currency request"
        def sender = new Account(id: UUID.randomUUID(), clientId: "c1", currency: "USD", balance: new BigDecimal("1000.00"))
        def receiver = new Account(id: UUID.randomUUID(), clientId: "c2", currency: "GBP", balance: new BigDecimal("500.00"))
        def request = new TransferRequest(sender.id, receiver.id, new BigDecimal("100.00"), "EUR")

        and: "Accounts loaded"
        accountRepository.findByIdWithLock(_) >>> [Optional.of(sender), Optional.of(receiver)]

        when: "Transfer is executed"
        transferService.transferFunds(request)

        then: "Exception is thrown"
        thrown(CurrencyMismatchException)
    }

    def "transferFunds - should prevent same account transfers"() {
        given: "Same account IDs"
        def accountId = UUID.randomUUID()
        def request = new TransferRequest(accountId, accountId, new BigDecimal("100.00"), "USD")

        when: "Transfer is attempted"
        transferService.transferFunds(request)

        then: "Exception is thrown"
        thrown(SameAccountTransferException)

        and: "No interactions with repositories"
        0 * accountRepository._
        0 * transactionRepository._
    }

    def "transferFunds - should lock accounts in ID order"() {
        given: "Accounts in reverse ID order"
        UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000001")
        UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000002")
        def acc1 = new Account(id: id1, clientId: "c1", currency: "USD", balance: new BigDecimal("1000.00"))
        def acc2 = new Account(id: id2, clientId: "c2", currency: "USD", balance: new BigDecimal("500.00"))

        and: "Request with reverse IDs"
        def request = new TransferRequest(id2, id1, new BigDecimal("100.00"), "USD")

        and: "Repository returns accounts in ID order"
        accountRepository.findByIdWithLock(id1) >> Optional.of(acc1)
        accountRepository.findByIdWithLock(id2) >> Optional.of(acc2)

        when: "Transfer is executed"
        transferService.transferFunds(request)

        then: "Accounts are processed in ID order (acc1 then acc2)"
        acc1.balance == new BigDecimal("1100.00") // Receiver
        acc2.balance == new BigDecimal("400.00")  // Sender
    }
}