package com.example.banking.exception;

public class ClientNotFoundException extends RuntimeException {
    public ClientNotFoundException(String id) {
        super("Client not found: " + id);
    }
}
