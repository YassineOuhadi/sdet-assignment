package com.example.deals.unit;

import com.example.deals.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        MDC.put("dealId", "D1");
        MDC.put("rowNum", "5");
    }

    @Nested
    @DisplayName("CSV Exceptions")
    class CsvExceptions {

        @Test
        @DisplayName("Should handle CsvParseException")
        void handleCsvParse() {
            CsvParseException ex = new CsvParseException("CSV error");
            ResponseEntity<Map<String, Object>> resp = handler.handleCsvParse(ex);

            assertThat(resp.getStatusCodeValue()).isEqualTo(400);
            assertThat(resp.getBody()).containsEntry("error", "CSV error")
                    .containsEntry("dealId", "D1")
                    .containsEntry("rowNum", "5");
        }
    }

    @Nested
    @DisplayName("Duplicate Deal Exceptions")
    class DuplicateDealExceptions {

        @Test
        @DisplayName("Should handle DuplicateDealException")
        void handleDuplicate() {
            DuplicateDealException ex = new DuplicateDealException("D2", "already exists");
            ResponseEntity<Map<String, Object>> resp = handler.handleDuplicate(ex);

            assertThat(resp.getStatusCodeValue()).isEqualTo(409);
            assertThat(resp.getBody()).containsEntry("error", "Deal D2: already exists")
                    .containsEntry("dealId", "D1")
                    .containsEntry("rowNum", "5");
        }
    }

    @Nested
    @DisplayName("Validation Exceptions")
    class ValidationExceptions {

        @Test
        @DisplayName("Should handle DealValidationException")
        void handleDealValidationException() {
            DealValidationException ex = new DealValidationException("Invalid deal");
            ResponseEntity<Map<String, Object>> resp = handler.handleValidation(ex);

            assertThat(resp.getStatusCodeValue()).isEqualTo(400);
            assertThat(resp.getBody()).containsEntry("error", "Invalid deal")
                    .containsEntry("dealId", "D1")
                    .containsEntry("rowNum", "5");
        }

        @Test
        @DisplayName("Should handle MethodArgumentNotValidException")
        void handleMethodArgumentNotValidException() {
            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            BindingResult br = mock(BindingResult.class);
            FieldError fe = new FieldError("dealRequest", "dealId", "must not be blank");

            when(ex.getBindingResult()).thenReturn(br);
            when(br.getFieldErrors()).thenReturn(List.of(fe));

            ResponseEntity<Map<String, Object>> resp = handler.handleValidation(ex);

            assertThat(resp.getStatusCodeValue()).isEqualTo(400);
            List<String> errors = (List<String>) resp.getBody().get("errors");
            assertThat(errors).contains("dealId must not be blank");
            assertThat(resp.getBody()).containsEntry("dealId", "D1")
                    .containsEntry("rowNum", "5");
        }
    }

    @Nested
    @DisplayName("Persistence Exceptions")
    class PersistenceExceptions {

        @Test
        @DisplayName("Should handle DealPersistenceException")
        void handlePersistence() {
            DealPersistenceException ex = new DealPersistenceException("DB down");
            ResponseEntity<Map<String, Object>> resp = handler.handlePersistence(ex);

            assertThat(resp.getStatusCodeValue()).isEqualTo(500);
            assertThat(resp.getBody()).containsEntry("error", "Persistence failed: DB down")
                    .containsEntry("dealId", "D1")
                    .containsEntry("rowNum", "5");
        }
    }

    @Nested
    @DisplayName("Generic Exceptions")
    class GenericExceptions {

        @Test
        @DisplayName("Should handle generic Exception")
        void handleAll() {
            Exception ex = new Exception("Generic error");
            ResponseEntity<Map<String, Object>> resp = handler.handleAll(ex);

            assertThat(resp.getStatusCodeValue()).isEqualTo(500);
            assertThat(resp.getBody()).containsEntry("error", "Generic error")
                    .containsEntry("dealId", "D1")
                    .containsEntry("rowNum", "5");
        }
    }
}