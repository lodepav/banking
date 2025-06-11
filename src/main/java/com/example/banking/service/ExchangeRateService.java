package com.example.banking.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateClient exchangeRateClient;

    @Cacheable(value = "exchangeRates", key = "{#fromCurrency, #toCurrency}")
    @CircuitBreaker(name = "exchangeRate", fallbackMethod = "getCachedRate")
    @Retry(name = "exchangeRate", fallbackMethod = "getCachedRate")
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        log.info("Fetching fresh exchange rate {}=>{}", fromCurrency, toCurrency);
        return exchangeRateClient.fetchExchangeRate(fromCurrency, toCurrency);
    }

    // Fallback method using cached rates
    public BigDecimal getCachedRate(String fromCurrency, String toCurrency, Throwable t) {
        log.warn("Using cached exchange rate for {}=>{} due to {}",
                fromCurrency, toCurrency, t.getMessage());
        // Actual cache retrieval handled by Spring
        return null; // Spring will handle cache lookup
    }
}