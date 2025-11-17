package com.example.deals.integration;

import com.example.deals.dto.DealRequest;
import com.example.deals.repository.DealRepository;
import com.example.deals.result.RowResult;
import com.example.deals.service.DealImportService;
import com.example.deals.parser.DealParser;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DealImportNoRollbackIT extends AbstractIntegrationTest {

    @Autowired
    private DealRepository repo;

    @Autowired
    private DealImportService service;

    @BeforeEach
    void cleanup() {
        repo.deleteAll();
    }

    @Test
    void importPartialRows_noRollbackOnFailure() {
        DealRequest valid1 = new DealRequest();
        valid1.setDealId("NR1");
        valid1.setFromCurrency("USD");
        valid1.setToCurrency("EUR");
        valid1.setTimestamp(Instant.now().toString());
        valid1.setAmountStr("100");

        DealRequest invalid = new DealRequest();
        invalid.setDealId("NR2");
        invalid.setFromCurrency("USD");
        invalid.setToCurrency("GBP");
        invalid.setTimestamp(Instant.now().toString());
        invalid.setAmountStr(""); // invalid

        DealRequest valid2 = new DealRequest();
        valid2.setDealId("NR3");
        valid2.setFromCurrency("EUR");
        valid2.setToCurrency("JPY");
        valid2.setTimestamp(Instant.now().toString());
        valid2.setAmountStr("200");

        Set<String> seen = new HashSet<>();
        RowResult r1 = service.importOneRow(new DealParser.RowData(1, valid1), seen);
        RowResult r2 = service.importOneRow(new DealParser.RowData(2, invalid), seen);
        RowResult r3 = service.importOneRow(new DealParser.RowData(3, valid2), seen);

        assertThat(r1.status()).isEqualTo("SUCCESS");
        assertThat(r2.status()).isEqualTo("FAILURE");
        assertThat(r3.status()).isEqualTo("SUCCESS");

        assertThat(repo.findByDealId("NR1")).isPresent();
        assertThat(repo.findByDealId("NR2")).isEmpty();
        assertThat(repo.findByDealId("NR3")).isPresent();

        assertThat(repo.count()).isEqualTo(2);
    }
}