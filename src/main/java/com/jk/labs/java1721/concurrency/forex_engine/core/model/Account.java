package com.jk.labs.java1721.concurrency.forex_engine.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity(name = "account")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @Column(name = "account_id", nullable = false, length = 50)
    private String id;

    @Column(name = "name", length = 250)
    private String name;

    @Column(name = "description", length = 250)
    private String description;

    @Column(name = "created_dt")
    private Instant timestamp;

    @Column(name = "status", length = 50)
    private String status;

}