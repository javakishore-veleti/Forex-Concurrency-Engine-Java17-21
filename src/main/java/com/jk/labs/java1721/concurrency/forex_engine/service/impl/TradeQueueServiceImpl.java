package com.jk.labs.java1721.concurrency.forex_engine.service.impl;

import com.jk.labs.java1721.concurrency.forex_engine.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeQueueServiceImpl implements TradeQueueService {

    private final TradeProducer tradeProducer;
    private final TradeValidator tradeValidator;
    private final TradeEnricher tradeEnricher;
    private final TradeSettler tradeSettler;
    private final TradeAuditor tradeAuditor;

    @Override
    public void startClassicBlockingQueuePipeline() {
        log.info("Starting classic blocking queue pipeline...");

        tradeProducer.start();
        tradeValidator.start();
        tradeEnricher.start();
        tradeSettler.start();
        tradeAuditor.start();

        log.info("Completed classic blocking queue pipeline...");
    }

    @Override
    public void stopClassicBlockingQueuePipeline() {
        log.info("Started Stopping classic blocking queue pipeline...");

        tradeProducer.stop();
        tradeValidator.stop();
        tradeEnricher.stop();
        tradeSettler.stop();
        tradeAuditor.stop();

        log.info("Completed Stopping classic blocking queue pipeline...");
    }

    @Override
    public void startVirtualThreadPipeline() {
        log.info("Starting virtual thread pipeline...");

        log.info("Completed virtual thread pipeline...");
    }
}
