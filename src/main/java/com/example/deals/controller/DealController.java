package com.example.deals.controller;

import com.example.deals.dto.DealResponse;
import com.example.deals.parser.DealParser;
import com.example.deals.result.RowResult;
import com.example.deals.service.DealImportService;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.*;

@RestController
@RequestMapping("/api/v1/deals")
public class DealController {

    private final DealParser parser;
    private final DealImportService importService;

    public DealController(DealParser parser, DealImportService importService) {
        this.parser = parser;
        this.importService = importService;
    }

    @PostMapping("/import")
    public ResponseEntity<?> importDeals(@RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "CSV file is required"));
        }

        if (!Objects.requireNonNull(file.getOriginalFilename()).toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only CSV files are allowed"));
        }

        try {
            // Parsing
            var rows = parser.parse(new InputStreamReader(file.getInputStream()));

            // Persist in service for validation, deduplication & import flow
            List<RowResult> results = importService.importRows(rows);

            return ResponseEntity.ok(Map.of("results", results));

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<DealResponse>> getAllDeals() {
        return ResponseEntity.ok(importService.getAllDealsDto());
    }

    @GetMapping("/{dealId}")
    public ResponseEntity<DealResponse> getDealById(@PathVariable String dealId) {
        return importService.getDealByIdDto(dealId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}