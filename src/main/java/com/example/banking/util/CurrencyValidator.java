package com.example.banking.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Currency;

public class CurrencyValidator
        implements ConstraintValidator<ValidCurrency, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            Currency.getInstance(value);
            return true;
        } catch (Exception _) {
            return false;
        }
    }
}