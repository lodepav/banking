package com.example.banking.service;

import com.example.banking.exception.ExchangeRateException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class ExchangeRateClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${exchange-rate.api.url:https://openexchangerates.org/api/latest.json}")
    private String apiUrl;

    public BigDecimal fetchExchangeRate(String fromCurrency, String toCurrency) {
        String url = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("app_Id", "my-app-id")
                .queryParam("base", fromCurrency)
                .queryParam("symbols", toCurrency)
                .queryParam("prettyprint", false)
                .queryParam("show_alternative", false)
                .toUriString();

        Map<?, ?> response = restTemplate.getForObject(url, Map.class);

        if (response != null && response.containsKey("rates")) {
            Map<?, ?> rates = (Map<?, ?>) response.get("rates");
            if (rates.containsKey(toCurrency)) {
                return new BigDecimal(rates.get(toCurrency).toString());
            }
        }
        throw new ExchangeRateException("Failed to retrieve exchange rate");
    }
}