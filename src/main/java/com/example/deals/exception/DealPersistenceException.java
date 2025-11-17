package com.example.deals.exception;

public class DealPersistenceException extends RuntimeException {
    public DealPersistenceException(String message) {
        super("Persistence failed: " + message);
    }
}