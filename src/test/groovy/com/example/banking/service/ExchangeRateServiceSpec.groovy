package com.example.banking.service

import com.example.banking.exception.ExchangeRateException
import com.github.benmanes.caffeine.cache.Caffeine
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Subject
import spock.util.concurrent.PollingConditions

import java.util.concurrent.TimeUnit

@SpringBootTest
@ContextConfiguration(classes = [TestConfig])
class ExchangeRateServiceSpec extends Specification {

    @SpringBean
    ExchangeRateClient exchangeRateClient = Mock()

    @Autowired
    @Subject
    ExchangeRateService exchangeRateService

    @Autowired
    CacheManager cacheManager

    @Configuration
    @EnableCaching
    static class TestConfig {
        @Bean
        @Primary
        CacheManager testCacheManager() {
            CaffeineCacheManager cacheManager = new CaffeineCacheManager("exchangeRates")
            cacheManager.setCaffeine(Caffeine.newBuilder()
                    .expireAfterWrite(5, TimeUnit.SECONDS) // Shorter TTL for tests
                    .maximumSize(1000))
            return cacheManager
        }

        @Bean
        ExchangeRateService exchangeRateService(ExchangeRateClient client) {
            return new ExchangeRateService(client)
        }
    }

    def setup() {
        // Clear cache before each test
        cacheManager.getCache("exchangeRates").clear()
    }

    def "should fetch exchange rate from client"() {
        given: "Mocked exchange rate"
        exchangeRateClient.fetchExchangeRate("USD", "EUR") >> new BigDecimal("0.92")

        when: "Requesting rate"
        BigDecimal rate = exchangeRateService.getExchangeRate("USD", "EUR")

        then: "Returns correct rate"
        rate == 0.92
    }
/* THIS PART DOESN'T WORK YET (OR ITS SETUP)
    def "should cache exchange rates"() {
        given: "Initial request"
        exchangeRateClient.fetchExchangeRate("USD", "GBP") >> new BigDecimal("0.78")
        exchangeRateService.getExchangeRate("USD", "GBP")

        when: "Same request again"
        BigDecimal cachedRate = exchangeRateService.getExchangeRate("USD", "GBP")

        then: "Uses cached value without client call"
        cachedRate == 0.78
        1 * exchangeRateClient.fetchExchangeRate("USD", "GBP") // Only first call
    }

    def "should respect cache TTL"() {
        given: "Mocked exchange rate"
        exchangeRateClient.fetchExchangeRate("EUR", "JPY") >> new BigDecimal("130.50")
        exchangeRateService.getExchangeRate("EUR", "JPY")

        when: "Wait for cache expiration"
        def conditions = new PollingConditions(timeout: 10, initialDelay: 1, delay: 1)
        conditions.eventually {
            // Get cache instance
            Cache cache = cacheManager.getCache("exchangeRates")
            // Create unique key for cache lookup
            def key = List.of("EUR", "JPY")
            // Verify cache entry is gone
            assert cache.get(key) == null
        }

        and: "Fetch rate again"
        BigDecimal freshRate = exchangeRateService.getExchangeRate("EUR", "JPY")

        then: "Fetches new rate from client"
        2 * exchangeRateClient.fetchExchangeRate("EUR", "JPY") >> 130.50
        freshRate == 130.50
    }

    def "should use stale cache when client fails"() {
        given: "Initial successful request"
        exchangeRateClient.fetchExchangeRate("GBP", "USD") >> new BigDecimal("1.30")
        exchangeRateService.getExchangeRate("GBP", "USD")

        and: "Client starts failing"
        exchangeRateClient.fetchExchangeRate("GBP", "USD") >>
                { throw new ExchangeRateException("Service down") }

        when: "Requesting rate during outage"
        BigDecimal staleRate = exchangeRateService.getExchangeRate("GBP", "USD")

        then: "Returns stale cached value"
        staleRate == 1.30
    }

    def "should throw exception when no cached value available"() {
        given: "No cached value and client fails"
        exchangeRateClient.fetchExchangeRate("CAD", "MXN") >>
                { throw new ExchangeRateException("Service down") }

        when: "Requesting rate"
        exchangeRateService.getExchangeRate("CAD", "MXN")

        then: "Throws exception"
        thrown(ExchangeRateException)
    } */
}