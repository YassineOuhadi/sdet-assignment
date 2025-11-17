package com.example.deals.result;

import com.example.deals.dto.DealRequest;

import java.util.List;

public record ParseResult(
        List<DealRequest> validRows,
        List<RowResult> parsingErrors
) {}
