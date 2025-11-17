package com.example.deals.dto;

import java.math.BigDecimal;

public class DealResponse {

    private String dealId;
    private String fromCurrency;
    private String toCurrency;
    private String timestamp;
    private BigDecimal amount;

    public DealResponse(String dealId, String fromCurrency, String toCurrency, String timestamp, BigDecimal amount) {
        this.dealId = dealId;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.timestamp = timestamp;
        this.amount = amount;
    }

    public String getDealId() { return dealId; }
    public String getFromCurrency() { return fromCurrency; }
    public String getToCurrency() { return toCurrency; }
    public String getTimestamp() { return timestamp; }
    public BigDecimal getAmount() { return amount; }

    public static DealResponse fromEntity(com.example.deals.model.Deal deal) {
        return new DealResponse(
                deal.getDealId(),
                deal.getFromCurrency(),
                deal.getToCurrency(),
                deal.getDealTimestamp().toString(),
                deal.getAmount()
        );
    }
}