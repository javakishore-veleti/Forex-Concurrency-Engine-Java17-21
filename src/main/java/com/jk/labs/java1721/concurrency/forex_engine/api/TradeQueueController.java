package com.jk.labs.java1721.concurrency.forex_engine.api;

import com.jk.labs.java1721.concurrency.forex_engine.core.dto.TradeRespDto;
import com.jk.labs.java1721.concurrency.forex_engine.service.TradeQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/concurrency", produces = "application/json")
@RequiredArgsConstructor
public class TradeQueueController {

    private final TradeQueueService tradeQueueService;

    // Starts the classic BlockingQueue pipeline
    @RequestMapping(path = "/blockingqueue/start", method = RequestMethod.GET)
    public ResponseEntity<TradeRespDto> blockingqueueStart() {
        TradeRespDto tradeRespDto = new TradeRespDto();

        tradeQueueService.startClassicBlockingQueuePipeline();

        tradeRespDto.setResponseMessage("Classic BlockingQueue pipeline started.");
        return ResponseEntity.ok(tradeRespDto);
    }

    // Stops workers (graceful shutdown)
    @RequestMapping(path = "/blockingqueue/stop", method = RequestMethod.GET)
    public ResponseEntity<TradeRespDto> blockingqueueStop() {
        TradeRespDto tradeRespDto = new TradeRespDto();

        tradeQueueService.stopClassicBlockingQueuePipeline();

        tradeRespDto.setResponseMessage("Classic BlockingQueue pipeline stopped.");
        return ResponseEntity.ok(tradeRespDto);
    }

    // Runs same logic with Virtual Threads (Java 21)
    @RequestMapping(path = "/virtualThreads/start", method = RequestMethod.GET)
    public ResponseEntity<TradeRespDto> virtualThreadsStart() {
        TradeRespDto tradeRespDto = new TradeRespDto();

        tradeQueueService.startVirtualThreadPipeline();

        tradeRespDto.setResponseMessage("Virtual Threads pipeline stopped.");
        return ResponseEntity.ok(tradeRespDto);
    }
}
