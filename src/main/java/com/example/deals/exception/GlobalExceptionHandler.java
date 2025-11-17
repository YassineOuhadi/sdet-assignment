package com.example.deals.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .collect(Collectors.toList());

        Map<String, Object> body = Map.of(
                "dealId", MDC.get("dealId"),
                "rowNum", MDC.get("rowNum"),
                "errors", errors
        );

        log.warn("Validation exception: {}", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(CsvParseException.class)
    public ResponseEntity<Map<String, Object>> handleCsvParse(CsvParseException ex) {
        Map<String, Object> body = Map.of(
                "dealId", MDC.get("dealId"),
                "rowNum", MDC.get("rowNum"),
                "error", ex.getMessage()
        );

        log.error("CSV parse exception: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(DuplicateDealException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateDealException ex) {
        Map<String, Object> body = Map.of(
                "dealId", MDC.get("dealId"),
                "rowNum", MDC.get("rowNum"),
                "error", ex.getMessage()
        );

        log.warn("Duplicate deal exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DealValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(DealValidationException ex) {
        Map<String, Object> body = Map.of(
                "dealId", MDC.get("dealId"),
                "rowNum", MDC.get("rowNum"),
                "error", ex.getMessage()
        );

        log.warn("Deal validation exception: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(DealPersistenceException.class)
    public ResponseEntity<Map<String, Object>> handlePersistence(DealPersistenceException ex) {
        Map<String, Object> body = Map.of(
                "dealId", MDC.get("dealId"),
                "rowNum", MDC.get("rowNum"),
                "error", ex.getMessage()
        );

        log.error("Deal persistence exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {
        Map<String, Object> body = Map.of(
                "dealId", MDC.get("dealId"),
                "rowNum", MDC.get("rowNum"),
                "error", ex.getMessage()
        );

        log.error("Unhandled exception: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}