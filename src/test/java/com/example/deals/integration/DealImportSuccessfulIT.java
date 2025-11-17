package com.example.deals.integration;

import com.example.deals.dto.DealRequest;
import com.example.deals.model.Deal;
import com.example.deals.repository.DealRepository;
import com.example.deals.result.RowResult;
import com.example.deals.service.DealImportService;
import com.example.deals.parser.DealParser;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DealImportSuccessfulIT extends AbstractIntegrationTest {

    @Autowired
    private DealRepository repo;

    @Autowired
    private DealImportService service;

    @BeforeEach
    void cleanup() {
        repo.deleteAll();
    }

    @Test
    void importSuccessful() {
        DealRequest req = new DealRequest();
        req.setDealId("S1");
        req.setFromCurrency("USD");
        req.setToCurrency("EUR");
        req.setTimestamp(Instant.now().toString());
        req.setAmountStr("1000");

        RowResult row = service.importOneRow(new DealParser.RowData(1, req), new HashSet<>());

        assertThat(row.status()).isEqualTo("SUCCESS");
        assertThat(repo.findByDealId("S1")).isPresent();
    }
}