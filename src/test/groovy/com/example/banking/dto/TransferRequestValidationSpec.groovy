package com.example.banking.dto

import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import spock.lang.Specification
import spock.lang.Unroll

class TransferRequestValidationSpec extends Specification {

    ValidatorFactory factory = Validation.buildDefaultValidatorFactory()
    Validator validator = factory.getValidator()

    @Unroll
    def "should validate TransferRequest with fromAccountId=#fromAccountId, toAccountId=#toAccountId, amount=#amount, currency=#currency"() {
        given: "a TransferRequest"
        TransferRequest request = new TransferRequest(
                fromAccountId,
                toAccountId,
                amount,
                currency
        )

        when: "the request is validated"
        def violations = validator.validate(request)

        then: "the number of violations matches the expected count"
        violations.size() == expectedViolationCount

        where:
        fromAccountId                  | toAccountId                  | amount        | currency | expectedViolationCount
        UUID.randomUUID()              | UUID.randomUUID()            | new BigDecimal("100.00") | "USD"     | 0
        null                           | UUID.randomUUID()            | new BigDecimal("100.00") | "USD"     | 1
        UUID.randomUUID()              | null                         | new BigDecimal("100.00") | "USD"     | 1
        UUID.randomUUID()              | UUID.randomUUID()            | new BigDecimal("-100.00")| "USD"     | 1
        UUID.randomUUID()              | UUID.randomUUID()            | new BigDecimal("100.00") | "US"      | 2
        UUID.randomUUID()              | UUID.randomUUID()            | new BigDecimal("100.00") | "USD"     | 0
    }
}
