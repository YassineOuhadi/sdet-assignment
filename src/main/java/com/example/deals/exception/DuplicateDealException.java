package com.example.deals.exception;

public class DuplicateDealException extends RuntimeException {
    public DuplicateDealException(String dealId, String message) {
        super("Deal " + dealId + ": " + message);
    }
}