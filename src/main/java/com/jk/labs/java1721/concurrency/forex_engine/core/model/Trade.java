package com.jk.labs.java1721.concurrency.forex_engine.core.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity(name = "trade")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trade {

    @Id
    @Column(name = "trade_id", nullable = false, length = 50)
    private String id;

    @Column(name = "account_id", length = 50)
    private String accountId;

    @Column(name = "from_currency", length = 50)
    private String fromCurrency;

    @Column(name = "to_currency", length = 50)
    private String toCurrency;

    @Column(name = "amount", precision = 19, scale = 8)
    private BigDecimal amount;

    @Column(name = "no_of_units")
    private Integer noOfUnits;

    @Column(name = "rate", precision = 19, scale = 8)
    private BigDecimal rate;

    @Column(name = "created_dt")
    private Instant timestamp;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "broker_id", nullable = false, length = 50)
    private String brokerId;
}