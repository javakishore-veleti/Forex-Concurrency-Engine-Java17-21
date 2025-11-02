package com.jk.labs.java1721.concurrency.forex_engine.core.dto;

import com.jk.labs.java1721.concurrency.forex_engine.core.model.Trade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeReqDto {

    private Trade trade;
    private List<Trade> trades;
    private String tradeId;
    private String accountId;
    private String brokerId;
    private String fromCurrency;
    private String toCurrency;
    private Integer noOfUnits;

}
