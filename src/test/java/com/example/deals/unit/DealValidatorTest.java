package com.example.deals.unit;

import com.example.deals.dto.DealRequest;
import com.example.deals.exception.DealValidationException;
import com.example.deals.validation.DealValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("DealValidator Unit Tests")
class DealValidatorTest {

    private DealValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DealValidator();
    }

    @Nested
    @DisplayName("Valid Deal")
    class ValidDeal {

        @Test
        @DisplayName("Should pass validation for correct deal")
        void validate_validDeal_doesNotThrow() {
            DealRequest req = new DealRequest();
            req.setDealId("D1");
            req.setFromCurrency("USD");
            req.setToCurrency("EUR");
            req.setTimestamp("2025-01-01T10:00:00Z");
            req.setAmountStr("1000");

            assertDoesNotThrow(() -> validator.validate(req));
        }
    }

    @Nested
    @DisplayName("DealId Validation")
    class DealIdValidation {

        @Test
        @DisplayName("Null dealId should throw exception")
        void validate_nullDealId_throws() {
            DealRequest req = new DealRequest();
            req.setDealId(null);
            req.setFromCurrency("USD");
            req.setToCurrency("EUR");
            req.setTimestamp("2025-01-01T10:00:00Z");
            req.setAmountStr("1000");

            assertThatThrownBy(() -> validator.validate(req))
                    .isInstanceOf(DealValidationException.class)
                    .hasMessageContaining("DealId is required");
        }

        @Test
        @DisplayName("Blank dealId should throw exception")
        void validate_blankDealId_throws() {
            DealRequest req = new DealRequest();
            req.setDealId("   ");
            req.setFromCurrency("USD");
            req.setToCurrency("EUR");
            req.setTimestamp("2025-01-01T10:00:00Z");
            req.setAmountStr("1000");

            assertThatThrownBy(() -> validator.validate(req))
                    .isInstanceOf(DealValidationException.class)
                    .hasMessageContaining("DealId is required");
        }
    }

    @Nested
    @DisplayName("Currency Validation")
    class CurrencyValidation {

        @Test
        @DisplayName("Invalid fromCurrency should throw exception")
        void validate_invalidCurrency_throws() {
            DealRequest req = new DealRequest();
            req.setDealId("D1");
            req.setFromCurrency("US");
            req.setToCurrency("EUR");
            req.setTimestamp("2025-01-01T10:00:00Z");
            req.setAmountStr("1000");

            assertThatThrownBy(() -> validator.validate(req))
                    .isInstanceOf(DealValidationException.class)
                    .hasMessageContaining("fromCurrency must be a 3-letter ISO code");
        }

        @Test
        @DisplayName("Null fromCurrency should throw exception")
        void validate_nullCurrency_throws() {
            DealRequest req = new DealRequest();
            req.setDealId("D1");
            req.setFromCurrency(null);
            req.setToCurrency("EUR");
            req.setTimestamp("2025-01-01T10:00:00Z");
            req.setAmountStr("1000");

            assertThatThrownBy(() -> validator.validate(req))
                    .isInstanceOf(DealValidationException.class)
                    .hasMessageContaining("fromCurrency must be a 3-letter ISO code");
        }
    }

    @Nested
    @DisplayName("Timestamp Validation")
    class TimestampValidation {

        @Test
        @DisplayName("Invalid timestamp format should throw exception")
        void validate_invalidTimestampFormat_throws() {
            DealRequest req = new DealRequest();
            req.setDealId("D1");
            req.setFromCurrency("USD");
            req.setToCurrency("EUR");
            req.setTimestamp("invalid");
            req.setAmountStr("1000");

            assertThatThrownBy(() -> validator.validate(req))
                    .isInstanceOf(DealValidationException.class)
                    .hasMessageContaining("Invalid timestamp format");
        }

        @Test
        @DisplayName("Future timestamp should throw exception")
        void validate_futureTimestamp_throws() {
            DealRequest req = new DealRequest();
            req.setDealId("D1");
            req.setFromCurrency("USD");
            req.setToCurrency("EUR");
            req.setTimestamp("2999-01-01T00:00:00Z");
            req.setAmountStr("1000");

            assertThatThrownBy(() -> validator.validate(req))
                    .isInstanceOf(DealValidationException.class)
                    .hasMessageContaining("Timestamp cannot be in the future");
        }

        @Test
        @DisplayName("Null timestamp should throw exception")
        void validate_nullTimestamp_throws() {
            DealRequest req = new DealRequest();
            req.setDealId("D1");
            req.setFromCurrency("USD");
            req.setToCurrency("EUR");
            req.setTimestamp(null);
            req.setAmountStr("1000");

            assertThatThrownBy(() -> validator.validate(req))
                    .isInstanceOf(DealValidationException.class)
                    .hasMessageContaining("Timestamp is required");
        }

        @Test
        @DisplayName("Blank timestamp should throw exception")
        void validate_blankTimestamp_throws() {
            DealRequest req = new DealRequest();
            req.setDealId("D1");
            req.setFromCurrency("USD");
            req.setToCurrency("EUR");
            req.setTimestamp("   ");
            req.setAmountStr("1000");

            assertThatThrownBy(() -> validator.validate(req))
                    .isInstanceOf(DealValidationException.class)
                    .hasMessageContaining("Timestamp is required");
        }
    }

    @Nested
    @DisplayName("Amount Validation")
    class AmountValidation {

        @Test
        @DisplayName("Null amount should throw exception")
        void validate_nullAmount_throws() {
            DealRequest req = new DealRequest();
            req.setDealId("D1");
            req.setFromCurrency("USD");
            req.setToCurrency("EUR");
            req.setTimestamp("2025-01-01T10:00:00Z");
            req.setAmountStr(null);

            assertThatThrownBy(() -> validator.validate(req))
                    .isInstanceOf(DealValidationException.class)
                    .hasMessageContaining("Amount is required");
        }

        @Test
        @DisplayName("Blank amount should throw exception")
        void validate_blankAmount_throws() {
            DealRequest req = new DealRequest();
            req.setDealId("D1");
            req.setFromCurrency("USD");
            req.setToCurrency("EUR");
            req.setTimestamp("2025-01-01T10:00:00Z");
            req.setAmountStr(" ");

            assertThatThrownBy(() -> validator.validate(req))
                    .isInstanceOf(DealValidationException.class)
                    .hasMessageContaining("Amount is required");
        }

        @Test
        @DisplayName("Negative amount should throw exception")
        void validate_negativeAmount_throws() {
            DealRequest req = new DealRequest();
            req.setDealId("D1");
            req.setFromCurrency("USD");
            req.setToCurrency("EUR");
            req.setTimestamp("2025-01-01T10:00:00Z");
            req.setAmountStr("-10");

            assertThatThrownBy(() -> validator.validate(req))
                    .isInstanceOf(DealValidationException.class)
                    .hasMessageContaining("Amount must be positive");
        }

        @Test
        @DisplayName("Invalid number format should throw exception")
        void validate_invalidAmountFormat_throws() {
            DealRequest req = new DealRequest();
            req.setDealId("D1");
            req.setFromCurrency("USD");
            req.setToCurrency("EUR");
            req.setTimestamp("2025-01-01T10:00:00Z");
            req.setAmountStr("abc");

            assertThatThrownBy(() -> validator.validate(req))
                    .isInstanceOf(DealValidationException.class)
                    .hasMessageContaining("Amount is not a valid number");
        }
    }
}