package com.jk.labs.java1721.concurrency.forex_engine.config;

import com.jk.labs.java1721.concurrency.forex_engine.core.model.Trade;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
@Data
public class TradeQueueManager {

    private static final int QUEUE_CAPACITY = 10000;
    /*
    A BlockingQueue is bounded when you specify a capacity:
    That means:
        The queue can hold at most 100 trades at any time.
        Each call to put() will insert an element only if there’s space available.
        Now, if the queue is already full (contains 100 trades),
        then put() will block — i.e. pause the current thread — until
        A consumer thread (like your TradeValidator) removes an element with take().
            That removal frees up a slot.
                Then the producer thread wakes up and continues.
                So “blocks if queue full” means:
                The producer thread is temporarily suspended (parked) until space becomes available in the queue.

     🧩 Why This Matters
        It’s built-in backpressure.
            In high-performance systems, you never want producers to flood memory faster than consumers can process.
            Let’s say:
                You’re producing 1,000 trades/sec.
                Validators can only process 500/sec.
                Without a bounded queue, trades would pile up in memory → OutOfMemoryError.
                With a bounded BlockingQueue:
                    The producer automatically slows down.
                    It will “block” on put() until validators catch up.

                That’s what we mean by backpressure — the system naturally self-throttles.

    ⚡ Alternative: Non-blocking Operations
    Sometimes, you don’t want the producer to wait forever.
    Then you can use offer():
        boolean success = validationQueue.offer(trade, 2, TimeUnit.SECONDS);

    That means:
        Try to enqueue for up to 2 seconds.
        If still full, give up (returns false).
        That’s non-blocking (bounded waiting) instead of put()’s unbounded blocking.

    🧩 Summary Table
    | Method                                    | Behavior when queue full                  | Use Case                                   |
    | ----------------------------------------- | ----------------------------------------- | ------------------------------------------ |
    | `put(E e)`                                | Blocks indefinitely until space available | Reliable pipelines (you want backpressure) |
    | `offer(E e)`                              | Returns immediately with false            | Drop or retry mechanism                    |
    | `offer(E e, long timeout, TimeUnit unit)` | Waits up to timeout                       | Controlled waiting                         |
    | `add(E e)`                                | Throws `IllegalStateException`            | Not used in concurrent systems             |

    validationQueue.put(trade):
        Thread-safe insertion into the shared queue.
        Blocks producer if consumers are slower.
        Implements natural flow control (backpressure).
        Prevents memory bloat or overload in high-throughput pipelines.
     */
    private static final BlockingQueue<Trade> tradeQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

    public static void addTrade(Trade trade) {
        try {
            tradeQueue.put(trade); // blocks if queue full
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to add trade to the queue", e);
        }
    }
}
