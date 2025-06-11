Feature: Fund Transfers
  As a banking customer
  I want to transfer funds between accounts
  So I can manage my finances

  Background:
    Given the following accounts exist:
      | clientId | accountId | currency | balance |
      | client-1 | acc-1     | USD      | 1000.00 |
      | client-1 | acc-2     | EUR      | 500.00  |
      | client-2 | acc-3     | GBP      | 750.00  |

  Scenario: Successful transfer in same currency
    When I transfer "100.00" "USD" from account "acc-1" to account "acc-1"
    Then the response status should be 400
    And the response should contain "Cannot transfer to the same account"

  Scenario: Successful transfer with currency conversion
    Given the exchange rate from "USD" to "EUR" is "0.92"
    When I transfer "100.00" "EUR" from account "acc-1" to account "acc-2"
    Then the response status should be 200
    And account "acc-1" should have balance "1000.00 - (100.00 / 0.92)" USD
    And account "acc-2" should have balance "600.00" EUR

  Scenario: Insufficient funds
    When I transfer "2000.00" "USD" from account "acc-1" to account "acc-3"
    Then the response status should be 400
    And the response should contain "insufficient funds"

  Scenario: Currency mismatch
    When I transfer "100.00" "GBP" from account "acc-1" to account "acc-2"
    Then the response status should be 400
    And the response should contain "Receiver account requires EUR"