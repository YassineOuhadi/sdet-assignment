package com.example.deals.service;

import com.example.deals.dto.DealRequest;
import com.example.deals.dto.DealResponse;
import com.example.deals.exception.DealValidationException;
import com.example.deals.model.Deal;
import com.example.deals.parser.DealParser;
import com.example.deals.repository.DealRepository;
import com.example.deals.result.RowResult;
import com.example.deals.validation.DealValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DealImportService {

    private static final Logger log = LoggerFactory.getLogger(DealImportService.class);

    private final DealRepository repository;
    private final DealValidator validator;

    public DealImportService(DealRepository repository, DealValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    public List<RowResult> importRows(List<DealParser.RowData> rows) {
        Set<String> seenIds = new HashSet<>();
        List<RowResult> results = new ArrayList<>();

        for (DealParser.RowData row : rows) {
            results.add(importOneRow(row, seenIds));
        }

        return results;
    }

    public RowResult importOneRow(DealParser.RowData row, Set<String> seenIds) {

        DealRequest req = row.request();
        int rowNum = row.rowNum();

        MDC.put("dealId", req.getDealId());
        MDC.put("rowNum", String.valueOf(rowNum));

        String dealId = req.getDealId();

        try {
            if (seenIds.contains(dealId)) {
                log.warn("Duplicate in file");
                return RowResult.duplicate(dealId, "Duplicate dealId in file");
            }

            seenIds.add(dealId);

            validator.validate(req);

            if (repository.findByDealId(dealId).isPresent()) {
                log.warn("Duplicate in DB");
                return RowResult.duplicate(dealId, "Deal already exists in DB");
            }

            repository.save(new Deal(req));

            log.info("Imported successfully");
            return RowResult.success(dealId);

        } catch (DealValidationException ex) {
            log.error("Validation failure: {}", ex.getMessage());
            return RowResult.failure(dealId, ex.getMessage());

        } catch (Exception ex) {
            log.error("Persistence error: {}", ex.getMessage());
            return RowResult.failure(dealId, "Database error: " + ex.getMessage());

        } finally {
            MDC.clear();
        }
    }

    public List<DealResponse> getAllDealsDto() {
        return repository.findAll().stream()
                .map(DealResponse::fromEntity)
                .toList();
    }

    public Optional<DealResponse> getDealByIdDto(String dealId) {
        return repository.findByDealId(dealId)
                .map(DealResponse::fromEntity);
    }
}