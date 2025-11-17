package com.example.deals.integration;

import com.example.deals.dto.DealRequest;
import com.example.deals.model.Deal;
import com.example.deals.repository.DealRepository;
import com.example.deals.result.RowResult;
import com.example.deals.service.DealImportService;
import com.example.deals.parser.DealParser;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DealImportDuplicateDbIT extends AbstractIntegrationTest {

    @Autowired
    private DealRepository repo;

    @Autowired
    private DealImportService service;

    @BeforeEach
    void cleanup() {
        repo.deleteAll();
    }

    @Test
    void importDuplicateDb() {
        Deal deal = new Deal();
        deal.setDealId("D1");
        deal.setFromCurrency("USD");
        deal.setToCurrency("EUR");
        deal.setDealTimestamp(Instant.now());
        deal.setAmount(BigDecimal.valueOf(500));
        repo.save(deal);

        DealRequest req = new DealRequest();
        req.setDealId("D1");
        req.setFromCurrency("USD");
        req.setToCurrency("EUR");
        req.setTimestamp(Instant.now().toString());
        req.setAmountStr("1000");

        RowResult row = service.importOneRow(new DealParser.RowData(2, req), new HashSet<>());

        assertThat(row.status()).isEqualTo("DUPLICATE");
        assertThat(repo.count()).isEqualTo(1);
    }
}
