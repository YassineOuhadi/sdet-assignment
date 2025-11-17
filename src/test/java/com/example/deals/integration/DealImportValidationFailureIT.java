package com.example.deals.integration;

import com.example.deals.dto.DealRequest;
import com.example.deals.repository.DealRepository;
import com.example.deals.result.RowResult;
import com.example.deals.service.DealImportService;
import com.example.deals.parser.DealParser;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DealImportValidationFailureIT extends AbstractIntegrationTest {

    @Autowired
    private DealRepository repo;

    @Autowired
    private DealImportService service;

    @BeforeEach
    void cleanup() {
        repo.deleteAll();
    }

    @Test
    void importValidationError() {
        DealRequest invalid = new DealRequest();
        invalid.setDealId("I1"); // missing currency & amount

        RowResult row = service.importOneRow(new DealParser.RowData(1, invalid), new HashSet<>());

        assertThat(row.status()).isEqualTo("FAILURE");
        assertThat(repo.count()).isEqualTo(0);
    }
}
