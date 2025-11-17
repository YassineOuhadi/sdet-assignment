package com.example.deals.unit;

import com.example.deals.dto.DealResponse;
import com.example.deals.dto.DealRequest;
import com.example.deals.model.Deal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Deal Layer Tests")
class DealTest {

    @Nested
    @DisplayName("Deal Entity Tests")
    class DealEntityTests {

        @Nested
        @DisplayName("Default Constructor")
        class DefaultConstructor {

            @Test
            @DisplayName("Setters and getters should work correctly")
            void settersGetters() {
                Deal deal = new Deal();
                deal.setId(1L);
                deal.setDealId("D123");
                deal.setFromCurrency("USD");
                deal.setToCurrency("EUR");
                deal.setDealTimestamp(Instant.parse("2025-11-15T10:00:00Z"));
                deal.setAmount(BigDecimal.valueOf(1000.50));

                assertEquals(1L, deal.getId());
                assertEquals("D123", deal.getDealId());
                assertEquals("USD", deal.getFromCurrency());
                assertEquals("EUR", deal.getToCurrency());
                assertEquals(Instant.parse("2025-11-15T10:00:00Z"), deal.getDealTimestamp());
                assertEquals(BigDecimal.valueOf(1000.50), deal.getAmount());
            }
        }

        @Nested
        @DisplayName("Constructor with DealRequest")
        class ConstructorWithRequest {

            @Test
            @DisplayName("Valid DealRequest should map correctly")
            void validDealRequest() {
                DealRequest req = new DealRequest();
                req.setDealId("D456");
                req.setFromCurrency("GBP");
                req.setToCurrency("JPY");
                req.setTimestamp("2025-11-15T12:30:00Z");
                req.setAmountStr("500.25");

                Deal deal = new Deal(req);

                assertEquals("D456", deal.getDealId());
                assertEquals("GBP", deal.getFromCurrency());
                assertEquals("JPY", deal.getToCurrency());
                assertEquals(Instant.parse("2025-11-15T12:30:00Z"), deal.getDealTimestamp());
                assertEquals(BigDecimal.valueOf(500.25), deal.getAmount());
            }

            @Test
            @DisplayName("Invalid amount should fallback to zero")
            void invalidAmountFallback() {
                DealRequest req = new DealRequest();
                req.setDealId("D789");
                req.setFromCurrency("USD");
                req.setToCurrency("EUR");
                req.setTimestamp("2025-11-15T12:30:00Z");
                req.setAmountStr("abc"); // Invalid number

                Deal deal = new Deal(req);

                assertEquals("D789", deal.getDealId());
                assertEquals("USD", deal.getFromCurrency());
                assertEquals("EUR", deal.getToCurrency());
                assertEquals(Instant.parse("2025-11-15T12:30:00Z"), deal.getDealTimestamp());
                assertEquals(BigDecimal.ZERO, deal.getAmount(), "Invalid amount should fallback to 0");
            }

            @Test
            @DisplayName("Invalid timestamp should fallback to epoch")
            void invalidTimestampFallback() {
                DealRequest req = new DealRequest();
                req.setDealId("D999");
                req.setFromCurrency("USD");
                req.setToCurrency("EUR");
                req.setTimestamp("not-a-timestamp");
                req.setAmountStr("100");

                Deal deal = new Deal(req);

                assertEquals("D999", deal.getDealId());
                assertEquals("USD", deal.getFromCurrency());
                assertEquals("EUR", deal.getToCurrency());
                assertEquals(Instant.EPOCH, deal.getDealTimestamp(), "Invalid timestamp should fallback to epoch");
                assertEquals(BigDecimal.valueOf(100), deal.getAmount());
            }
        }
    }

    @Nested
    @DisplayName("DealResponse DTO Tests")
    class DealResponseTests {

        @Nested
        @DisplayName("Constructor Tests")
        class ConstructorTests {

            @Test
            void constructorAndGetters_returnCorrectValues() {
                DealResponse resp = new DealResponse(
                        "D123",
                        "USD",
                        "EUR",
                        "2025-11-15T10:00:00Z",
                        BigDecimal.valueOf(1000.50)
                );

                assertThat(resp.getDealId()).isEqualTo("D123");
                assertThat(resp.getFromCurrency()).isEqualTo("USD");
                assertThat(resp.getToCurrency()).isEqualTo("EUR");
                assertThat(resp.getTimestamp()).isEqualTo("2025-11-15T10:00:00Z");
                assertThat(resp.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000.50));
            }
        }

        @Nested
        @DisplayName("FromEntity Tests")
        class FromEntityTests {

            @Test
            void fromEntity_createsCorrectResponse() {
                Deal deal = new Deal();
                deal.setDealId("D456");
                deal.setFromCurrency("GBP");
                deal.setToCurrency("JPY");
                deal.setDealTimestamp(Instant.parse("2025-11-15T12:30:00Z"));
                deal.setAmount(BigDecimal.valueOf(500.25));

                DealResponse resp = DealResponse.fromEntity(deal);

                assertThat(resp.getDealId()).isEqualTo("D456");
                assertThat(resp.getFromCurrency()).isEqualTo("GBP");
                assertThat(resp.getToCurrency()).isEqualTo("JPY");
                assertThat(resp.getTimestamp()).isEqualTo("2025-11-15T12:30:00Z");
                assertThat(resp.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500.25));
            }
        }
    }

    @Nested
    @DisplayName("DealRequest edge cases")
    class DealRequestEdgeCases {

        @Test
        @DisplayName("Null amountStr should fallback to zero")
        void nullAmountStrFallback() {
            DealRequest req = new DealRequest();
            req.setDealId("D101");
            req.setFromCurrency("USD");
            req.setToCurrency("EUR");
            req.setTimestamp("2025-11-15T10:00:00Z");
            req.setAmountStr(null);

            Deal deal = new Deal(req);

            assertThat(deal.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Blank amountStr should fallback to zero")
        void blankAmountStrFallback() {
            DealRequest req = new DealRequest();
            req.setDealId("D102");
            req.setFromCurrency("USD");
            req.setToCurrency("EUR");
            req.setTimestamp("2025-11-15T10:00:00Z");
            req.setAmountStr("   ");

            Deal deal = new Deal(req);

            assertThat(deal.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}