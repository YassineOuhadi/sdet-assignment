package com.example.deals.model;

import com.example.deals.dto.DealRequest;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "deals", uniqueConstraints = @UniqueConstraint(columnNames = {"deal_id"}))
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deal_id", nullable = false, unique = true)
    private String dealId;

    @Column(name = "from_currency", nullable = false, length = 3)
    private String fromCurrency;

    @Column(name = "to_currency", nullable = false, length = 3)
    private String toCurrency;

    @Column(name = "deal_timestamp", nullable = false)
    private Instant dealTimestamp;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    public Deal() {}

    public Deal(DealRequest req) {
        this.dealId = req.getDealId();
        this.fromCurrency = req.getFromCurrency();
        this.toCurrency = req.getToCurrency();

        Instant parsedTimestamp;
        try {
            parsedTimestamp = Instant.parse(req.getTimestamp());
        } catch (Exception e) {
            parsedTimestamp = Instant.EPOCH; // fallback
        }
        this.dealTimestamp = parsedTimestamp;

        BigDecimal parsedAmount;
        try {
            if (req.getAmountStr() == null || req.getAmountStr().isBlank()) {
                parsedAmount = BigDecimal.ZERO; // fallback
            } else {
                parsedAmount = new BigDecimal(req.getAmountStr());
            }
        } catch (NumberFormatException e) {
            parsedAmount = BigDecimal.ZERO; // fallback
        }
        this.amount = parsedAmount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDealId() {
        return dealId;
    }

    public void setDealId(String dealId) {
        this.dealId = dealId;
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public void setFromCurrency(String fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public String getToCurrency() {
        return toCurrency;
    }

    public void setToCurrency(String toCurrency) {
        this.toCurrency = toCurrency;
    }

    public Instant getDealTimestamp() {
        return dealTimestamp;
    }

    public void setDealTimestamp(Instant dealTimestamp) {
        this.dealTimestamp = dealTimestamp;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}