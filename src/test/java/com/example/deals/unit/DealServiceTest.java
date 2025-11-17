package com.example.deals.unit;

import com.example.deals.dto.DealRequest;
import com.example.deals.dto.DealResponse;
import com.example.deals.model.Deal;
import com.example.deals.parser.DealParser;
import com.example.deals.repository.DealRepository;
import com.example.deals.result.RowResult;
import com.example.deals.service.DealImportService;
import com.example.deals.validation.DealValidator;
import com.example.deals.exception.DealValidationException;

import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DealServiceTest {

    private DealRepository repository;
    private DealValidator validator;
    private DealImportService service;

    @BeforeEach
    void setup() {
        repository = mock(DealRepository.class);
        validator = mock(DealValidator.class);
        service = new DealImportService(repository, validator);
    }

    @Nested
    @DisplayName("Import One Row Tests")
    class ImportOneRowTests {

        @Test
        @DisplayName("Successful import")
        void success() {
            DealRequest req = new DealRequest();
            req.setDealId("D1");
            req.setFromCurrency("USD");
            req.setToCurrency("EUR");
            req.setTimestamp("2025-11-15T10:00:00Z");
            req.setAmountStr("100");

            DealParser.RowData row = new DealParser.RowData(1, req);
            when(repository.findByDealId("D1")).thenReturn(Optional.empty());

            RowResult result = service.importOneRow(row, new HashSet<>());

            assertThat(result.status()).isEqualTo("SUCCESS");
            verify(validator).validate(req);
            verify(repository).save(any(Deal.class));
        }

        @Test
        @DisplayName("Duplicate in file")
        void duplicateInFile() {
            DealRequest req = new DealRequest();
            req.setDealId("D1");
            DealParser.RowData row = new DealParser.RowData(1, req);

            Set<String> seen = new HashSet<>();
            seen.add("D1");

            RowResult result = service.importOneRow(row, seen);

            assertThat(result.status()).isEqualTo("DUPLICATE");
            verifyNoInteractions(validator);
            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("Duplicate in DB")
        void duplicateInDb() {
            DealRequest req = new DealRequest();
            req.setDealId("D1");
            DealParser.RowData row = new DealParser.RowData(1, req);

            when(repository.findByDealId("D1")).thenReturn(Optional.of(new Deal()));

            RowResult result = service.importOneRow(row, new HashSet<>());

            assertThat(result.status()).isEqualTo("DUPLICATE");
            verify(validator).validate(req);
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Validation failure")
        void validationFailure() {
            DealRequest req = new DealRequest();
            req.setDealId("D1");
            DealParser.RowData row = new DealParser.RowData(1, req);

            doThrow(new DealValidationException("Invalid deal")).when(validator).validate(req);

            RowResult result = service.importOneRow(row, new HashSet<>());

            assertThat(result.status()).isEqualTo("FAILURE");
            assertThat(result.message()).isEqualTo("Invalid deal");
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Persistence failure")
        void persistenceFailure() {
            DealRequest req = new DealRequest();
            req.setDealId("D1");
            req.setFromCurrency("USD");
            req.setToCurrency("EUR");
            req.setTimestamp("2025-11-15T10:00:00Z");
            req.setAmountStr("100");

            DealParser.RowData row = new DealParser.RowData(1, req);

            when(repository.findByDealId("D1")).thenReturn(Optional.empty());
            doThrow(new RuntimeException("DB down")).when(repository).save(any());

            RowResult result = service.importOneRow(row, new HashSet<>());

            assertThat(result.status()).isEqualTo("FAILURE");
        }
    }

    @Nested
    @DisplayName("Import Multiple Rows Tests")
    class ImportRowsTests {

        @Test
        @DisplayName("Import multiple rows successfully")
        void multipleRows() {
            DealRequest req1 = new DealRequest();
            req1.setDealId("D1");
            DealRequest req2 = new DealRequest();
            req2.setDealId("D2");

            List<DealParser.RowData> rows = List.of(
                    new DealParser.RowData(1, req1),
                    new DealParser.RowData(2, req2)
            );

            when(repository.findByDealId(anyString())).thenReturn(Optional.empty());

            List<RowResult> results = service.importRows(rows);

            assertThat(results).hasSize(2);
            assertThat(results.get(0).status()).isEqualTo("SUCCESS");
            assertThat(results.get(1).status()).isEqualTo("SUCCESS");
        }
    }

    @Nested
    @DisplayName("Retrieval Tests")
    class RetrievalTests {

        @Test
        @DisplayName("Get all deals as DTOs")
        void getAllDealsDto_returnsMapped() {
            Deal deal = new Deal();
            deal.setDealId("D1");
            deal.setFromCurrency("USD");
            deal.setToCurrency("EUR");
            deal.setDealTimestamp(Instant.now());
            deal.setAmount(BigDecimal.TEN);

            when(repository.findAll()).thenReturn(List.of(deal));

            List<DealResponse> dtos = service.getAllDealsDto();

            assertThat(dtos).hasSize(1);
            assertThat(dtos.get(0).getDealId()).isEqualTo("D1");
        }

        @Test
        @DisplayName("Get deal by ID found")
        void getDealByIdDto_returnsMapped() {
            Deal deal = new Deal();
            deal.setDealId("D1");
            deal.setFromCurrency("USD");
            deal.setToCurrency("EUR");
            deal.setDealTimestamp(Instant.now());
            deal.setAmount(BigDecimal.TEN);

            when(repository.findByDealId("D1")).thenReturn(Optional.of(deal));

            Optional<DealResponse> dto = service.getDealByIdDto("D1");

            assertThat(dto).isPresent();
            assertThat(dto.get().getDealId()).isEqualTo("D1");
        }

        @Test
        @DisplayName("Get deal by ID not found")
        void getDealByIdDto_notFound() {
            when(repository.findByDealId("D2")).thenReturn(Optional.empty());

            Optional<DealResponse> dto = service.getDealByIdDto("D2");

            assertThat(dto).isEmpty();
        }
    }

    @Nested
    @DisplayName("Partial Success Imports")
    class PartialSuccessImports {

        @Test
        @DisplayName("Import valid and fail invalid deals")
        void importDeals_partialSuccess() {
            DealRequest valid = new DealRequest();
            valid.setDealId("V1");
            valid.setFromCurrency("USD");
            valid.setToCurrency("EUR");
            valid.setTimestamp(Instant.now().toString());
            valid.setAmountStr("1000");

            DealRequest invalid = new DealRequest();
            invalid.setDealId("I1");

            doNothing().when(validator).validate(valid);
            doThrow(new DealValidationException("Invalid deal")).when(validator).validate(invalid);

            Set<String> seen = new HashSet<>();
            RowResult row1 = service.importOneRow(new DealParser.RowData(1, valid), seen);
            RowResult row2 = service.importOneRow(new DealParser.RowData(2, invalid), seen);

            assertThat(row1.status()).isEqualTo("SUCCESS");
            assertThat(row2.status()).isEqualTo("FAILURE");
            assertThat(row2.message()).isEqualTo("Invalid deal");

            verify(repository, times(1)).save(any());
        }
    }
}