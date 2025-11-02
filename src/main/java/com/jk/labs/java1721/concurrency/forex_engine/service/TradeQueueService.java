package com.jk.labs.java1721.concurrency.forex_engine.service;

public interface TradeQueueService {
    void startClassicBlockingQueuePipeline();

    void stopClassicBlockingQueuePipeline();

    void startVirtualThreadPipeline();
}
