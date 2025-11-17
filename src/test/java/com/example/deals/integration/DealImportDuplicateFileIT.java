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
class DealImportDuplicateFileIT extends AbstractIntegrationTest {

    @Autowired
    private DealRepository repo;

    @Autowired
    private DealImportService service;

    @BeforeEach
    void cleanup() {
        repo.deleteAll();
    }

    @Test
    void importDuplicateInFile() {
        DealRequest req1 = new DealRequest();
        req1.setDealId("F1");
        req1.setFromCurrency("USD");
        req1.setToCurrency("EUR");
        req1.setTimestamp(Instant.now().toString());
        req1.setAmountStr("1000");

        DealRequest req2 = new DealRequest();
        req2.setDealId("F1");
        req2.setFromCurrency("USD");
        req2.setToCurrency("EUR");
        req2.setTimestamp(Instant.now().toString());
        req2.setAmountStr("2000");

        Set<String> seen = new HashSet<>();
        RowResult r1 = service.importOneRow(new DealParser.RowData(1, req1), seen);
        RowResult r2 = service.importOneRow(new DealParser.RowData(2, req2), seen);

        assertThat(r1.status()).isEqualTo("SUCCESS");
        assertThat(r2.status()).isEqualTo("DUPLICATE");
        assertThat(repo.count()).isEqualTo(1);
    }
}
