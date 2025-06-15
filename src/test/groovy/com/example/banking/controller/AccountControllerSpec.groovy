package com.example.banking.controller

import com.example.banking.domain.Account
import com.example.banking.service.AccountService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import java.time.Instant

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = AccountController)
class AccountControllerSpec extends Specification {

    @Autowired
    MockMvc mvc

    @SpringBean
    AccountService accountService = Stub()

    def "GET /clients/{clientId}/accounts returns 200 and list of AccountResponse"() {
        given:
        String clientId = "client-123"
        def now = Instant.now()
        UUID id1 = UUID.randomUUID()
        UUID id2 = UUID.randomUUID()

        accountService.getClientAccounts(clientId) >> [
                Account.builder()
                        .id(id1)
                        .clientId(clientId)
                        .currency("USD")
                        .balance(new BigDecimal("100.00"))
                        .createdAt(now)
                        .build(),
                Account.builder()
                        .id(id2)
                        .clientId(clientId)
                        .currency("EUR")
                        .balance(new BigDecimal("250.50"))
                        .createdAt(now)
                        .build()
        ]

        expect:
        mvc.perform(get("/clients/$clientId/accounts")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath('$[0].id').value(id1.toString()))
                .andExpect(jsonPath('$[0].currency').value("USD"))
                .andExpect(jsonPath('$[0].balance').value(100.00))
                .andExpect(jsonPath('$[1].id').value(id2.toString()))
                .andExpect(jsonPath('$[1].currency').value("EUR"))
                .andExpect(jsonPath('$[1].balance').value(250.50))
    }

    def "GET /clients/{clientId}/accounts returns empty list when none"() {
        given:
        String clientId = "client-456"
        accountService.getClientAccounts(clientId) >> []

        expect:
        mvc.perform(get("/clients/$clientId/accounts")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$').isArray())
                .andExpect(jsonPath('$').isEmpty())
    }
}
