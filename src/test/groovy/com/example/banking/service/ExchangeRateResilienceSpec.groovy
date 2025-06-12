package com.example.banking.service

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@SpringBootTest
class ExchangeRateResilienceSpec extends Specification {

    @Autowired
    ExchangeRateService exchangeRateService

    @Autowired
    CircuitBreakerRegistry circuitBreakerRegistry

    def "should use cached rates when external service fails"() {
        given: "Circuit breaker in closed state"
        def cb = circuitBreakerRegistry.circuitBreaker("exchangeRate")
        cb.reset()

        and: "Successful rate call"
        BigDecimal initialRate = exchangeRateService.getExchangeRate("USD", "EUR")

        when: "External service starts failing"
        // Simulate service failure (implementation depends on your HTTP client)
        mockServerFailure()

        and: "Fetch rate during outage"
        BigDecimal cachedRate = exchangeRateService.getExchangeRate("USD", "EUR")

        then: "Should return cached value"
        cachedRate == initialRate

        and: "Circuit breaker eventually opens"
        new PollingConditions(timeout: 10).eventually {
            cb.state == CircuitBreaker.State.OPEN
        }
    }

    def "should recover after service restoration"() {
        given: "Circuit breaker open"
        def cb = circuitBreakerRegistry.circuitBreaker("exchangeRate")
        cb.transitionToOpenState()

        when: "Service is restored"
        mockServerFailure(false)

        and: "Wait for half-open state"
        Thread.sleep(cb.circuitBreakerConfig.waitDurationInOpenState.toMillis() + 1000)

        and: "Make successful call"
        BigDecimal rate = exchangeRateService.getExchangeRate("USD", "EUR")

        then: "Circuit breaker closes"
        cb.state == CircuitBreaker.State.CLOSED
        rate != null
    }

    private void mockServerFailure() {
        // Implementation depends on HTTP client
        // Example for MockRestServiceServer:
        // server.expect(any()).andRespond(withServerError())
    }
}