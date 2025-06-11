package com.example.banking.util;

import com.example.banking.exception.InvalidCurrencyException;
import java.util.Currency;

public class CurrencyValidator {

    private CurrencyValidator(){}

    public static void validateCurrencyCode(String currencyCode) {
        try {
            Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException | NullPointerException _) {
            throw new InvalidCurrencyException(
                    "Invalid currency code: " + currencyCode +
                            ". Must be a valid ISO 4217 code."
            );
        }
    }
}