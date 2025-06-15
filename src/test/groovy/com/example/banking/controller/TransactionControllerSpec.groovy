package com.example.banking.controller

import com.example.banking.domain.Account
import com.example.banking.domain.AccountTransaction
import com.example.banking.service.TransactionService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import java.time.Instant

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = TransactionController)
class TransactionControllerSpec extends Specification {

    @Autowired
    MockMvc mvc

    @SpringBean
    TransactionService transactionService = Stub()

    def "GET /accounts/{accountId}/transactions returns paged list"() {
        given:
        UUID accountId = UUID.randomUUID()
        def acct = Account.builder().id(accountId).clientId("client-x").currency("USD")
                .balance(new BigDecimal("100")).createdAt(Instant.now()).build()
        def now = Instant.now()
        def tx1 = AccountTransaction.builder()
                .id(UUID.randomUUID()).account(acct).amount(new BigDecimal("10.00"))
                .currency("USD").type(AccountTransaction.TransactionType.TRANSFER_IN)
                .createdAt(now).description("desc1").correlationId(UUID.randomUUID()).build()
        def tx2 = AccountTransaction.builder()
                .id(UUID.randomUUID()).account(acct).amount(new BigDecimal("20.50"))
                .currency("USD").type(AccountTransaction.TransactionType.TRANSFER_OUT)
                .createdAt(now).description("desc2").correlationId(UUID.randomUUID()).build()
        Page<AccountTransaction> page = new PageImpl<>([tx1, tx2], PageRequest.of(0,2), 5)

        transactionService.getAccountTransactions(accountId, PageRequest.of(0,2)) >> page

        expect:
        mvc.perform(get("/accounts/$accountId/transactions")
                .param("offset", "0")
                .param("limit", "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath('$[0].id').value(tx1.id.toString()))
                .andExpect(jsonPath('$[0].type').value('TRANSFER_IN'))
                .andExpect(jsonPath('$[0].amount').value(10.00))
                .andExpect(jsonPath('$[1].id').value(tx2.id.toString()))
                .andExpect(jsonPath('$[1].type').value('TRANSFER_OUT'))
                .andExpect(jsonPath('$[1].amount').value(20.50))
    }

    def "GET with invalid UUID returns 400"() {
        expect:
        mvc.perform(get("/accounts/not-uuid/transactions")
                .param("offset", "0")
                .param("limit", "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
    }

    def "GET with negative limit returns 400"() {
        given:
        UUID accountId = UUID.randomUUID()

        expect:
        mvc.perform(get("/accounts/$accountId/transactions")
                .param("offset", "0")
                .param("limit", "-1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
    }
}
