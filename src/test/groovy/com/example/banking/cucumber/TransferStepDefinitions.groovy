package com.example.banking.cucumber


import com.example.banking.repository.AccountRepository
import com.example.banking.dto.TransferRequest
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

import java.math.BigDecimal

class TransferStepDefinitions {

    @Autowired
    TestRestTemplate restTemplate

    @Autowired
    AccountRepository accountRepository

    def response
    def exchangeRates = [:]

    @Given("the exchange rate from {string} to {string} is {bigdecimal}")
    void setExchangeRate(String from, String to, BigDecimal rate) {
        exchangeRates["$from-$to"] = rate
    }

    @When("I transfer {string} {string} from account {string} to account {string}")
    void transferFunds(String amount, String currency, String fromAccId, String toAccId) {
        def request = new TransferRequest(
                accountId(fromAccId),
                accountId(toAccId),
                new BigDecimal(amount),
                currency
        )

        response = restTemplate.exchange(
                "/transfers",
                HttpMethod.POST,
                new HttpEntity<>(request),
                String.class
        )
    }

    @Then("the response status should be {int}")
    void verifyStatus(int status) {
        response.statusCode == HttpStatus.valueOf(status)
    }

    @Then("account {string} should have balance {string} {string}")
    void verifyBalance(String accountId, String expectedBalanceExpr, String currency) {
        def account = accountRepository.findByAccountId(accountId(accountId)).get()
        def expectedBalance = evaluateExpression(expectedBalanceExpr)

        assert account.balance == expectedBalance
        assert account.currency == currency
    }

    private UUID accountId(String testId) {
        // Map test IDs to real UUIDs
    }

    private BigDecimal evaluateExpression(String expr) {
        // Simple expression evaluation
        if (expr.contains("-")) {
            def parts = expr.split(" - ")
            return new BigDecimal(parts[0]) - new BigDecimal(parts[1])
        }
        return new BigDecimal(expr)
    }
}