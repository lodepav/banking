package com.example.banking.controller

import com.example.banking.dto.TransferRequest
import com.example.banking.dto.TransferResult
import com.example.banking.service.TransferService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = TransferController)
class TransferControllerSpec extends Specification {

    @Autowired
    MockMvc mvc

    @SpringBean
    TransferService transferService = Stub()

    def "POST /transfers successfully transfers funds between accounts"() {
        given:
        UUID fromAccountId = UUID.randomUUID()
        UUID toAccountId = UUID.randomUUID()
        TransferRequest request = new TransferRequest(
                fromAccountId,
                toAccountId,
                new BigDecimal("150.75"),
                "USD"
        )
        TransferResult transferResult = new TransferResult(
                UUID.randomUUID(),
                new BigDecimal("150.75"),
                "USD",
                new BigDecimal("150.75"),
                "USD",
                new BigDecimal("1.00"),
                new BigDecimal("850.75"),
                new BigDecimal("150.75")
        )
        transferService.transferFunds(request) >> transferResult

        when:
        def response = mvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"fromAccountId": "' + fromAccountId.toString() + '", "toAccountId": "' + toAccountId.toString() + '", "amount": 150.75, "currency": "USD"}'))

        then:
        response.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath('$.correlationId').exists())
                .andExpect(jsonPath('$.debitedAmount').value("150.75"))
                .andExpect(jsonPath('$.senderCurrency').value("USD"))
                .andExpect(jsonPath('$.creditedAmount').value("150.75"))
                .andExpect(jsonPath('$.receiverCurrency').value("USD"))
                .andExpect(jsonPath('$.exchangeRate').value("1.00"))
                .andExpect(jsonPath('$.senderNewBalance').value("850.75"))
                .andExpect(jsonPath('$.receiverNewBalance').value("150.75"))
    }

    def "POST /transfers returns 400 for invalid transfer request"() {
        given:
        TransferRequest invalidRequest = new TransferRequest(
                null,
                null,
                new BigDecimal("-100.00"),
                "USD"
        )

        when:
        def response = mvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"fromAccountId": null, "toAccountId": null, "amount": -100.00, "currency": "USD"}'))

        then:
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.message').value("toAccountId: must not be null; amount: must be greater than 0; fromAccountId: must not be null"))
    }
}
