package com.example.banking.integration

import com.example.banking.domain.Account
import com.example.banking.dto.TransferRequest
import com.example.banking.exception.SameAccountTransferException
import com.example.banking.repository.AccountRepository
import com.example.banking.repository.TransactionRepository
import com.example.banking.service.TransferService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

@SpringBootTest
@ActiveProfiles("test")
class TransferIntegrationSpec extends Specification {

    @Autowired
    TransferService transferService

    @Autowired
    AccountRepository accountRepository

    @Autowired
    TransactionRepository transactionRepository

    def "should maintain consistency during concurrent transfers"() {
        given: "An account with initial balance"
        def account = accountRepository.save(
                new Account(clientId: "c1", currency: "USD", balance: new BigDecimal("1000.00"))
        )

        and: "Concurrent transfer requests"
        def requests = (1..10).collect {
            new TransferRequest(account.id, account.id, new BigDecimal("10.00"), "USD")
        }

        when: "Executing transfers concurrently"
        def results = requests.parallelStream().map { req ->
            try {
                transferService.transferFunds(req)
                "SUCCESS"
            } catch (SameAccountTransferException ignored) {
                "FAILURE"
            }
        }.toList()

        then: "Only one transfer should succeed (same account prevention)"
        results.count { it == "SUCCESS" } == 1
        results.count { it == "FAILURE" } == 9

        and: "Balance remains unchanged"
        def updatedAccount = accountRepository.findById(account.id).get()
        updatedAccount.balance == new BigDecimal("1000.00")
    }

    def "should record transactions atomically"() {
        given: "Two accounts"
        def sender = accountRepository.save(
                new Account(clientId: "c1", currency: "USD", balance: new BigDecimal("1000.00"))
        )
        def receiver = accountRepository.save(
                new Account(clientId: "c2", currency: "USD", balance: new BigDecimal("500.00"))
        )
        def request = new TransferRequest(sender.id, receiver.id, new BigDecimal("100.00"), "USD")

        when: "Transfer is executed"
        transferService.transferFunds(request)

        then: "Balances are updated"
        def updatedSender = accountRepository.findById(sender.id).get()
        def updatedReceiver = accountRepository.findById(receiver.id).get()
        updatedSender.balance == new BigDecimal("900.00")
        updatedReceiver.balance == new BigDecimal("600.00")

        and: "Transactions are recorded"
        def pageable = PageRequest.of(0, 10)
        def senderTxns = transactionRepository.findTransactionsByAccountId(sender.id, pageable)
        def receiverTxns = transactionRepository.findTransactionsByAccountId(receiver.id, pageable)
        senderTxns.size() == 1
        receiverTxns.size() == 1
        senderTxns[0].amount == new BigDecimal("-100.00")
        receiverTxns[0].amount == new BigDecimal("100.00")
    }
}