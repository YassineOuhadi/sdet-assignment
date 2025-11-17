package com.example.deals.mock;

import com.example.deals.controller.DealController;
import com.example.deals.dto.DealResponse;
import com.example.deals.exception.DealPersistenceException;
import com.example.deals.parser.DealParser;
import com.example.deals.result.RowResult;
import com.example.deals.service.DealImportService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DealController.class)
class DealControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DealParser parser;

    @MockBean
    private DealImportService importService;

    @Test
    void importDeals_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "deals.csv",
                "text/csv",
                "dealId,fromCurrency,toCurrency,amount,timestamp\nD1,USD,EUR,100,2025-11-15T12:00:00"
                        .getBytes(StandardCharsets.UTF_8)
        );

        when(parser.parse(any(InputStreamReader.class))).thenReturn(List.of());
        when(importService.importRows(any())).thenReturn(
                List.of(RowResult.success("D1"))
        );

        mockMvc.perform(multipart("/api/v1/deals/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    void importDeals_nonCsv_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "badfile.txt",
                "text/plain",
                "data".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/deals/import").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only CSV files are allowed"));
    }

    @Test
    void importDeals_noFile_returns400() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("file", "", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/api/v1/deals/import").file(empty))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("CSV file is required"));
    }

    @Test
    void importDeals_serviceThrows500() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "deals.csv",
                "text/csv",
                "data".getBytes()
        );

        when(parser.parse(any())).thenReturn(Collections.emptyList());
        when(importService.importRows(any())).thenThrow(new DealPersistenceException("DB error"));

        mockMvc.perform(multipart("/api/v1/deals/import").file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Persistence failed: DB error"));
    }

    @Test
    void health_returnsUp() throws Exception {
        mockMvc.perform(get("/api/v1/deals/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void getAllDeals_returnsList() throws Exception {
        when(importService.getAllDealsDto()).thenReturn(
                List.of(new DealResponse("ID1", "USD", "EUR","2025-11-15T12:00:00", BigDecimal.valueOf(200)))
        );

        mockMvc.perform(get("/api/v1/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dealId").value("ID1"));
    }

    @Test
    void getAllDeals_empty_returns200() throws Exception {
        when(importService.getAllDealsDto()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getDealById_found() throws Exception {
        when(importService.getDealByIdDto("D1")).thenReturn(
                Optional.of(new DealResponse("D1", "USD", "EUR", "2025-10-01T10:00:00", BigDecimal.valueOf(200)))
        );

        mockMvc.perform(get("/api/v1/deals/D1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dealId").value("D1"));
    }

    @Test
    void getDealById_notFound() throws Exception {
        when(importService.getDealByIdDto("Missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/deals/Missing"))
                .andExpect(status().isNotFound());
    }
}