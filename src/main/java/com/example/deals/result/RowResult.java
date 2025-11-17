package com.example.deals.result;

public record RowResult(String dealId, String status, String message) {

    public static RowResult success(String dealId) {
        return new RowResult(dealId, "SUCCESS", null);
    }

    public static RowResult failure(String dealId, String msg) {
        return new RowResult(dealId, "FAILURE", msg);
    }

    public static RowResult duplicate(String dealId, String msg) {
        return new RowResult(dealId, "DUPLICATE", msg);
    }
}
