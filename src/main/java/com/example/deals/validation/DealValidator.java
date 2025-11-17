package com.example.deals.validation;

import com.example.deals.dto.DealRequest;
import com.example.deals.exception.DealValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

@Component
public class DealValidator {

    private final Validator beanValidator =
            Validation.buildDefaultValidatorFactory().getValidator();

    public void validate(DealRequest req) {
        validateBean(req);

//        if (req.getDealId() == null || req.getDealId().isBlank()) {
//            throw new DealValidationException("DealId is required");
//        }

        validateIsoCurrency(req.getFromCurrency(), "fromCurrency");
        validateIsoCurrency(req.getToCurrency(), "toCurrency");

        validateTimestamp(req.getTimestamp());

        validateAmount(req.getAmountStr());
    }

    private void validateBean(DealRequest req) {
        Set<ConstraintViolation<DealRequest>> violations = beanValidator.validate(req);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            violations.forEach(v -> sb.append(v.getMessage()).append("; "));
            throw new DealValidationException(sb.toString());
        }
    }

    private void validateIsoCurrency(String currency, String field) {
        if (currency == null || !currency.matches("[A-Z]{3}")) {
            throw new DealValidationException(field + " must be a 3-letter ISO code");
        }
    }

    private void validateTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            throw new DealValidationException("Timestamp is required");
        }

        Instant dealTime;
        try {
            dealTime = Instant.parse(timestamp);
        } catch (Exception e) {
            throw new DealValidationException("Invalid timestamp format: " + timestamp);
        }

        if (dealTime.isAfter(Instant.now())) {
            throw new DealValidationException("Timestamp cannot be in the future: " + timestamp);
        }
    }

    private void validateAmount(String amountStr) {
        if (amountStr == null || amountStr.isBlank()) {
            throw new DealValidationException("Amount is required");
        }
        try {
            double val = Double.parseDouble(amountStr);
            if (val <= 0) {
                throw new DealValidationException("Amount must be positive");
            }
        } catch (NumberFormatException e) {
            throw new DealValidationException("Amount is not a valid number: " + amountStr);
        }
    }
}