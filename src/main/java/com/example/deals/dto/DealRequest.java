package com.example.deals.dto;

import com.opencsv.bean.CsvBindByPosition;
import jakarta.validation.constraints.NotBlank;

public class DealRequest {

    @CsvBindByPosition(position = 0)
    @NotBlank(message="DealId is required")
    private String dealId;

    @CsvBindByPosition(position = 1)
    private String fromCurrency;

    @CsvBindByPosition(position = 2)
    private String toCurrency;

    @CsvBindByPosition(position = 3)
    private String timestamp;

    @CsvBindByPosition(position = 4)
    private String amountStr;

    public DealRequest() {
    }

    public String getDealId() { return dealId; }
    public void setDealId(String dealId) { this.dealId = dealId; }

    public String getFromCurrency() { return fromCurrency; }
    public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }

    public String getToCurrency() { return toCurrency; }
    public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getAmountStr() { return amountStr; }
    public void setAmountStr(String amountStr) { this.amountStr = amountStr; }
}