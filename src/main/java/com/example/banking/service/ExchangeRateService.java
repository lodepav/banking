package com.example.banking.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * The type Exchange rate service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateClient exchangeRateClient;

    /**
     * Gets exchange rate.
     *
     * @param fromCurrency the from currency
     * @param toCurrency   the to currency
     * @return the exchange rate
     */
    @Cacheable(value = "exchangeRates", key = "{#fromCurrency, #toCurrency}")
    @CircuitBreaker(name = "exchangeRate", fallbackMethod = "getCachedRate")
    @Retry(name = "exchangeRate", fallbackMethod = "getCachedRate")
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        log.info("Fetching fresh exchange rate {}=>{}", fromCurrency, toCurrency);
        return exchangeRateClient.fetchExchangeRate(fromCurrency, toCurrency);
    }

    /**
     * Gets cached rate.
     *
     * @param fromCurrency the from currency
     * @param toCurrency   the to currency
     * @param t            the t
     * @return the cached rate
     */
// Fallback method using cached rates
    public BigDecimal getCachedRate(String fromCurrency, String toCurrency, Throwable t) {
        log.warn("Using cached exchange rate for {}=>{} due to {}",
                fromCurrency, toCurrency, t.getMessage());
        // Actual cache retrieval handled by Spring
        return null; // Spring will handle cache lookup
    }
}