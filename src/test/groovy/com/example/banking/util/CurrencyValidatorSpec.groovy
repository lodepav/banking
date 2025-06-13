package com.example.banking.util

import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest
class CurrencyValidatorSpec extends Specification {

    @Autowired
    Validator validator

    def validatorUnderTest = new CurrencyValidator()

    def "standalone isValid should accept valid and reject invalid"() {
        expect:
        validatorUnderTest.isValid(validCode, Mock(ConstraintValidatorContext)) == expected

        where:
        validCode | expected
        'USD'     | true
        'EUR'     | true
        'JPY'     | true
        'XXX'     | true  // ISO includes "XXX" as no currency transaction
        null      | false
        ''        | false
        'INVALID' | false
        '123'     | false
    }

    @Unroll
    def "integration: validator pipeline rejects '#code' as invalid"() {
        given:
        def bean = new TestCurrencyBean(currency: code)

        when:
        Set<ConstraintViolation<TestCurrencyBean>> violations = validator.validate(bean)

        then:
        (violations.size() > 0) == invalid

        where:
        code      | invalid
        'USD'     | false
        null      | true
        'FOO'     | true
    }

    static class TestCurrencyBean {
        @ValidCurrency
        String currency
    }

    @Configuration
    static class ValidatorConfig {
        @Bean
        LocalValidatorFactoryBean validator() {
            new LocalValidatorFactoryBean()
        }
    }
}
