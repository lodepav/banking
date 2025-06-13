package com.example.banking.cucumber;

import com.example.banking.domain.Account;
import com.example.banking.dto.TransferRequest;
import com.example.banking.dto.TransferResponse;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*
import org.springframework.integration.test.context.SpringIntegrationTest;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class TransferStepDefinitions /*extends SpringIntegrationTest*/ {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private ResponseEntity<?> response;
    private final Map<String, BigDecimal> exchangeRates = new ConcurrentHashMap<>();
    private final Map<String, UUID> accountIdMap = new ConcurrentHashMap<>();
    private final Map<String, String> accountCurrencies = new ConcurrentHashMap<>();

    @Before
    public void setup() {
        // Clear data before each scenario
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        accountIdMap.clear();
        accountCurrencies.clear();
        exchangeRates.clear();
    }

    @Given("the following accounts exist:")
    public void the_following_accounts_exist(List<Map<String, String>> accounts) {
        for (Map<String, String> account : accounts) {
            Account acc = new Account();
            acc.setClientId(account.get("clientId"));
            acc.setCurrency(account.get("currency"));
            acc.setBalance(new BigDecimal(account.get("balance")));
            Account saved = accountRepository.save(acc);

            // Store mapping between test ID and real UUID
            accountIdMap.put(account.get("accountId"), saved.getId());
            accountCurrencies.put(account.get("accountId"), account.get("currency"));
        }
    }

    @Given("the exchange rate from {string} to {string} is {bigdecimal}")
    public void set_exchange_rate(String from, String to, BigDecimal rate) {
        exchangeRates.put(from + "-" + to, rate);
    }

    @When("I transfer {string} {string} from account {string} to account {string}")
    public void transfer_funds(String amount, String currency, String fromAccId, String toAccId) {
        UUID fromAccountId = getAccountId(fromAccId);
        UUID toAccountId = getAccountId(toAccId);

        TransferRequest request = new TransferRequest(
                fromAccountId,
                toAccountId,
                new BigDecimal(amount),
                currency
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-KEY", "test-key"); // Add if security is enabled

        HttpEntity<TransferRequest> entity = new HttpEntity<>(request, headers);

        response = restTemplate.exchange(
                "/transfers",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<TransferResponse>() {}
        );
    }

    @Then("the response status should be {int}")
    public void verify_status(int status) {
        assert response.getStatusCode().value() == status;
    }

    @Then("the response should contain {string}")
    public void response_contains(String expectedContent) {
        Object body = response.getBody();
        assert body != null : "Response body is null";

        if (body instanceof String) {
            assert ((String) body).contains(expectedContent);
        } else if (body instanceof TransferResponse) {
            TransferResponse tr = (TransferResponse) body;
            // Check if any field contains the expected content
            boolean contains = tr.correlationId().toString().contains(expectedContent) ||
                    tr.debitedAmount().toString().contains(expectedContent) ||
                    tr.senderCurrency().contains(expectedContent) ||
                    tr.creditedAmount().toString().contains(expectedContent) ||
                    tr.receiverCurrency().contains(expectedContent) ||
                    tr.exchangeRate().toString().contains(expectedContent) ||
                    tr.senderNewBalance().toString().contains(expectedContent) ||
                    tr.receiverNewBalance().toString().contains(expectedContent);

            assert contains : "Response body doesn't contain: " + expectedContent;
        } else {
            throw new AssertionError("Unsupported response type: " + body.getClass());
        }
    }

    @Then("account {string} should have balance {string} {string}")
    public void verify_balance(String accountId, String expectedBalanceExpr, String currency) {
        Account account = accountRepository.findById(getAccountId(accountId))
                .orElseThrow(() -> new AssertionError("Account not found: " + accountId));

        BigDecimal expectedBalance = evaluateExpression(expectedBalanceExpr);

        assert account.getBalance().compareTo(expectedBalance) == 0 :
                "Expected balance: " + expectedBalance + ", Actual: " + account.getBalance();

        assert account.getCurrency().equals(currency) :
                "Expected currency: " + currency + ", Actual: " + account.getCurrency();
    }

    @Then("the transfer response should show debited {string} {string} from {string}")
    public void verify_debited_amount(String amount, String currency, String accountId) {
        TransferResponse tr = (TransferResponse) response.getBody();
        assert tr != null : "TransferResponse is null";

        assert tr.debitedAmount().compareTo(new BigDecimal(amount)) == 0 :
                "Expected debited: " + amount + ", Actual: " + tr.debitedAmount();

        assert tr.senderCurrency().equals(currency) :
                "Expected currency: " + currency + ", Actual: " + tr.senderCurrency();

        assert tr.senderNewBalance().equals(
                accountRepository.findById(getAccountId(accountId))
                        .orElseThrow().getBalance()
        );
    }

    @Then("the transfer response should show credited {string} {string} to {string}")
    public void verify_credited_amount(String amount, String currency, String accountId) {
        TransferResponse tr = (TransferResponse) response.getBody();
        assert tr != null : "TransferResponse is null";

        assert tr.creditedAmount().compareTo(new BigDecimal(amount)) == 0 :
                "Expected credited: " + amount + ", Actual: " + tr.creditedAmount();

        assert tr.receiverCurrency().equals(currency) :
                "Expected currency: " + currency + ", Actual: " + tr.receiverCurrency();

        assert tr.receiverNewBalance().equals(
                accountRepository.findById(getAccountId(accountId))
                        .orElseThrow().getBalance()
        );
    }

    @Then("the transfer response should show exchange rate {string}")
    public void verify_exchange_rate(String rate) {
        TransferResponse tr = (TransferResponse) response.getBody();
        assert tr != null : "TransferResponse is null";

        assert tr.exchangeRate().compareTo(new BigDecimal(rate)) == 0 :
                "Expected rate: " + rate + ", Actual: " + tr.exchangeRate();
    }

    private UUID getAccountId(String testId) {
        return Optional.ofNullable(accountIdMap.get(testId))
                .orElseThrow(() -> new IllegalArgumentException("Unknown account ID: " + testId));
    }

    private BigDecimal evaluateExpression(String expr) {
        if (expr.contains("+")) {
            String[] parts = expr.split("\\+");
            return new BigDecimal(parts[0].trim()).add(new BigDecimal(parts[1].trim()));
        }
        if (expr.contains("-")) {
            String[] parts = expr.split("-");
            return new BigDecimal(parts[0].trim()).subtract(new BigDecimal(parts[1].trim()));
        }
        if (expr.contains("*")) {
            String[] parts = expr.split("\\*");
            return new BigDecimal(parts[0].trim()).multiply(new BigDecimal(parts[1].trim()));
        }
        if (expr.contains("/")) {
            String[] parts = expr.split("/");
            return new BigDecimal(parts[0].trim()).divide(
                    new BigDecimal(parts[1].trim()), 2, RoundingMode.HALF_EVEN);
        }
        return new BigDecimal(expr.trim());
    }
}