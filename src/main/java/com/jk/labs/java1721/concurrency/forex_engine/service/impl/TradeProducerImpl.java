package com.jk.labs.java1721.concurrency.forex_engine.service.impl;

import com.jk.labs.java1721.concurrency.forex_engine.config.TradeQueueManager;
import com.jk.labs.java1721.concurrency.forex_engine.core.model.Trade;
import com.jk.labs.java1721.concurrency.forex_engine.service.TradeProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

@SuppressWarnings("CommentedOutCode")
@Service
@Slf4j
public class TradeProducerImpl implements TradeProducer {

    // private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture<?> tradeProducerTask;

    /*
    By default, every @Service bean in Spring is a singleton — one instance per application context.

    So, when you call:
    tradeProducer.start(); and tradeProducer.stop();
    from your controller (through TradeQueueServiceImpl), both methods operate on the same object, e.g.: TradeProducerImpl@4e3f0a8a

    That means:
        The running flag belongs to this single bean instance.
        The ExecutorService field (executorService) also belongs to that same instance.

    How the Volatile Flag Works in This Context:

    Because it’s the same object, you might ask: “Why even need volatile if it’s the same instance?”
    The key: thread visibility, not object identity.

    When you call stop() later:
        The controller thread (main request thread) sets running = false.
        The background producer thread (different CPU core) might be using a cached value of running in its thread-local CPU cache.
        So the volatile keyword ensures the producer thread sees the update immediately, even though both methods are on the same object.
        It’s not about multiple objects — it’s about multiple threads accessing the same object concurrently

    Why This Is a Clean and Safe Design:
    This pattern — a single Spring bean with an internal “engine loop” — is actually a classic control mechanism in Spring-based systems.
        Part	Purpose
        Singleton bean (@Service)	Manages lifecycle and dependencies
        volatile boolean running	Thread-safe control flag
        ExecutorService	Runs the long-lived worker asynchronously
        start()	Initializes and submits worker
        stop()	Gracefully shuts down worker thread

        ✅ It’s clean
        ✅ It’s observable (easy to add metrics)
        ✅ It’s Spring-compatible (one instance coordinating background work)

        🚫 When It Becomes Unsafe

        It becomes tricky if:
            You call start() multiple times quickly — spawning multiple loops accidentally.
            → Mitigate by checking if (running) return; (which we did ✅)

            You use prototype-scoped beans for producers — each would have its own flag and executor.
            → That leads to coordination chaos.

            You forget to call shutdownNow() or shutdown() — background threads hang after app close.

            | Question                                              | Answer                                                                                             |
            | ----------------------------------------------------- | -------------------------------------------------------------------------------------------------- |
            | Are `start()` and `stop()` acting on the same object? | Yes — same Spring singleton bean                                                                   |
            | Then why `volatile`?                                  | Because multiple **threads** access the same field concurrently, and `volatile` ensures visibility |
            | Is it safe?                                           | Yes — as long as you guard double-start and properly shut down the executor                        |
            | Could it be improved later?                           | Yes — using Structured Concurrency or Reactor for lifecycle-scoped tasks                           |

     */
    private volatile boolean running;

    /*
    Executors.newSingleThreadExecutor() guarantees:
        - Exactly one active thread producing at a time.
        - If that thread dies, the executor replaces it automatically.
        - FIFO task submission order.
        - Simpler control and predictable logs.

    Why not newCachedThreadPool()?
        - newCachedThreadPool() creates unbounded threads for new tasks.
        - Here, you’re running a continuous loop (not short tasks), so it would just spawn and hold threads forever.
        - You’d end up with runaway thread creation if the producer were resubmitted (bad idea for a continuous producer loop).

     So for a long-running, single-responsibility producer, newSingleThreadExecutor() is safe and controlled.

     🧩 3️⃣ When to Prefer newCachedThreadPool()

        You’d use newCachedThreadPool() in cases like:
            - Many short-lived tasks that come and go rapidly.
            - Unpredictable spikes in workload (temporary burstiness).
            - You want the pool to automatically recycle idle threads.
                For example:
                ExecutorService executor = Executors.newCachedThreadPool();
                for (int i = 0; i < 1000; i++) {
                    executor.submit(() -> handleTrade(trade));
                }
            - Each task is small, and the pool adjusts dynamically.
            - But for your producer loop (one continuous thread producing forever), you don’t want a dynamic pool.

            | Concern               | What we did                           | Why                                                                                               |
            | --------------------- | ------------------------------------- | ------------------------------------------------------------------------------------------------- |
            | Thread-safe stop flag | `volatile boolean running`            | Guarantees visibility of stop signal between threads                                              |
            | Executor type         | `Executors.newSingleThreadExecutor()` | One dedicated producer thread, predictable, auto-restart if dies                                  |
            | Avoided               | `newCachedThreadPool()`               | Would create many unnecessary threads for a continuous loop                                       |
            | Future alternative    | Virtual thread executor (Java 21)     | Replaces all of this cleanly — `Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory())` |

     */
    @Override
    public void start() {
        if (running) {
            return;
        }

        running = true;
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        tradeProducerTask = scheduledExecutorService.scheduleAtFixedRate(
                this::produceTradeSafe, 0, 10, java.util.concurrent.TimeUnit.SECONDS
        );
        // executorService = Executors.newSingleThreadExecutor();
        // executorService.submit(this::produceTradeSafe);
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }

        log.info("STARTED Classic trade producer stopping");
        running = false;

        this.tradeProducerTask.cancel(true);
        this.tradeProducerTask = null;

        this.scheduledExecutorService.shutdownNow();
        this.scheduledExecutorService = null;
        // executorService.shutdownNow();

        log.info("COMPLETED Classic trade producer stopped and references cleared for GC.");
    }

    public void produceTradeSafe() {
        if (!running) {
            return;
        }

        Random random = new Random();

        Trade trade = new Trade();
        trade.setId(UUID.randomUUID().toString());
        trade.setAccountId(UUID.randomUUID().toString());
        trade.setBrokerId(UUID.randomUUID().toString());
        trade.setFromCurrency("USD");
        trade.setToCurrency("EUR");
        trade.setRate(BigDecimal.valueOf(random.nextDouble()));
        trade.setStatus("NEW");
        trade.setNoOfUnits(random.nextInt(1000));
        trade.setAmount(BigDecimal.valueOf(random.nextDouble() * 10000));

        /*
        method call will block (halt) if the queue is full.
        the calling thread will pause right there and not return until
        a consumer thread removes at least one trade from the queue.
        This producer thread is paused, not killed — it’s parked efficiently by the JVM (no CPU usage).
        You don’t have to write wait/notify logic — BlockingQueue handles it internally.
        This is intentional — it provides built-in backpressure.

        🧩 What if You Don’t Want It to Block?
        If you want TradeQueueManager.addTrade(trade) to return immediately, you can use:
        Option 1: Try with timeout

        boolean success = TradeQueueManager.offerTrade(trade, 2, TimeUnit.SECONDS);
        if (!success) {
            log.warn("Trade dropped due to full queue: {}", trade.getId());
        }
        Option 2: Drop or retry logic
        boolean success = TradeQueueManager.offerTrade(trade);
        if (!success) {
            // handle full queue (e.g., drop, log, retry later)
             // Maybe retry later or persist to a dead-letter queue
             // This prevents the thread from halting but requires you to decide what to do when the system is overloaded.

         TradeQueueManager.addTrade(trade) will not complete and will be blocked (halted)
         if the queue is full and no consumer has removed an element yet.

          It resumes automatically once a consumer calls take() and frees space.
         */
        TradeQueueManager.addTrade(trade);

        log.info("Produced trade: {}", trade.getId());
    }
}
