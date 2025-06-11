package com.example.banking.exception;

public class ExchangeRateException extends RuntimeException {
    public ExchangeRateException(String message) {
        super(message);
    }
}
