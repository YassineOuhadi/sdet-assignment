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
class DealImportPartialSuccessIT extends AbstractIntegrationTest {

    @Autowired
    private DealRepository repo;

    @Autowired
    private DealImportService service;

    @BeforeEach
    void cleanup() {
        repo.deleteAll();
    }

    @Test
    void partialSuccessImport() {
        DealRequest valid = new DealRequest();
        valid.setDealId("P1");
        valid.setFromCurrency("USD");
        valid.setToCurrency("EUR");
        valid.setTimestamp(Instant.now().toString());
        valid.setAmountStr("1000");

        DealRequest invalid = new DealRequest();
        invalid.setDealId("P2"); // missing fields

        Set<String> seen = new HashSet<>();
        RowResult r1 = service.importOneRow(new DealParser.RowData(1, valid), seen);
        RowResult r2 = service.importOneRow(new DealParser.RowData(2, invalid), seen);

        assertThat(r1.status()).isEqualTo("SUCCESS");
        assertThat(r2.status()).isEqualTo("FAILURE");
        assertThat(repo.count()).isEqualTo(1);
    }
}
