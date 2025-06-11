package com.example.banking.exception;

public class InvalidCurrencyException extends RuntimeException {
    public InvalidCurrencyException(String message) {
        super(message);
    }
}
