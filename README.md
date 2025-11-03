# Forex Trade Processing Platform (Java 17 and Java 21 Concurrency Mastery)

Goal: Build a modular, production-grade simulation of a real-time Forex Trade Processing Engine that demonstrates every
major concurrency and parallelism concept in modern Java (JDK 17 and JDK 21).
This project provides hands-on mastery of both traditional concurrency patterns (Java 17) and modern structured
concurrency paradigms (Java 21).

## Overview

This project models a high-throughput Forex trading platform that ingests, validates, enriches, settles, and audits
trades concurrently.
Each stage of the pipeline is backed by different concurrency primitives and collections, and every module demonstrates
a distinct aspect of blocking, coordination, and parallel execution.

## Architecture Overview

forex-processor/
├─ core-api/ → Domain models and base contracts
├─ queue-engine/ → BlockingQueue-driven trade pipeline
├─ concurrent-cache/ → Concurrent data stores and locks
├─ async-tasks/ → Async enrichment and parallel analytics
├─ coordination/ → Thread synchronizers and coordination patterns
├─ metrics-monitor/ → Atomic counters, metrics, and monitoring
└─ application-runner/ → Spring Boot entry point and integration

Each module runs independently but integrates through a shared trade bus of concurrent collections and queues.

## Business Scenario

Multiple brokers continuously submit FOREX trades (e.g., USD→EUR, GBP→JPY).
Trades go through five pipeline stages:

1. Intake (Producers) — trades generated concurrently.
2. Validation — schema & rule checks.
3. Enrichment — add FX rates via async tasks.
4. Settlement — update balances atomically.
5. Audit — asynchronously log and reconcile trades.

The system continuously operates, throttles itself using bounded queues, and safely shuts down using poison pills and
latches.

# Section 1: Java 17 Implementation

This section demonstrates the classic concurrency primitives, executors, collections, atomics, and coordination
mechanisms available up to Java 17.

## Core Blocking & Synchronization Primitives

Includes all BlockingQueue types, locks, synchronizers, and coordination tools such as Latch, Barrier, Semaphore,
Exchanger, and Phaser. Each is demonstrated with real business logic (trade intake, enrichment, settlement, and audit).

## Thread Pool & Task Management

All types of ExecutorService, ThreadPoolExecutor tuning, ScheduledExecutorService, ForkJoinPool, and parallel streams
are used.
Each pipeline stage operates in a dedicated executor, and backpressure is enforced through bounded queues.

## CompletableFuture & Asynchronous Programming

Non-blocking asynchronous tasks handle enrichment (e.g., FX rate lookups) using supplyAsync, thenApply, thenCompose, and
allOf/anyOf combinations.

## Concurrent Collections

The system uses ConcurrentHashMap, CopyOnWriteArrayList, ConcurrentLinkedQueue, ConcurrentLinkedDeque,
ConcurrentSkipListMap, and ConcurrentSkipListSet to manage shared state safely.

## Atomic & Low-Level Concurrency

AtomicInteger, AtomicLong, LongAdder, DoubleAdder, AtomicBoolean, AtomicReference, and VarHandle are used for atomic
counters, performance metrics, and concurrent data structure optimization.

## Coordination and Monitoring

CountDownLatch, Semaphore, Phaser, CyclicBarrier, Condition, and Exchanger control the synchronization flow between
worker pools.
Metrics are tracked using LongAdder and printed every 10 seconds by a ScheduledExecutorService.

# Section 2: Java 21 Modern Extensions

Java 21 introduces Structured Concurrency and Virtual Threads. This section evolves the same system using these
features.

## Structured Concurrency

The coordination module introduces StructuredTaskScope to replace manual latches, barriers, and phasers.
Tasks such as validation, enrichment, and settlement run within structured scopes that handle cancellation, failure
propagation, and lifecycle control automatically.

Example concept:

try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
Future<Trade> validated = scope.fork(() -> validate(trade));
Future<Trade> enriched = scope.fork(() -> enrich(validated.get()));
scope.join();
scope.throwIfFailed();
settle(enriched.resultNow());
}

## Virtual Threads

The queue-engine and async-tasks modules leverage Virtual Threads to replace traditional thread pools.
Every trade can now be processed in its own lightweight thread, drastically simplifying concurrency management while
scaling to thousands of concurrent trades.

Implementation concept:

ThreadFactory factory = Thread.ofVirtual().name("trade-worker-", 0).factory();
ExecutorService virtualExecutor = Executors.newThreadPerTaskExecutor(factory);

## Reactive Streams and Flow API

The async-tasks module introduces Flow.Publisher and Flow.Subscriber for streaming audit and monitoring data.
This demonstrates non-blocking message passing and backpressure management using Java's built-in reactive streams API.

## Integration and Backward Compatibility

Both Java 17 and 21 runtimes are supported:

- Java 17: default baseline build, uses classic thread pools and synchronizers.
- Java 21: enables Virtual Threads and Structured Concurrency through profile activation (e.g.,
  spring.profiles.active=java21).

## Module Summary

| Module             | Focus                | Key Concepts                                    |
|--------------------|----------------------|-------------------------------------------------|
| core-api           | Domain models        | Trade, Account, enums, DTOs                     |
| queue-engine       | Multi-stage pipeline | BlockingQueue, ExecutorService, Virtual Threads |
| concurrent-cache   | Shared state         | ConcurrentHashMap, StampedLock, VarHandle       |
| async-tasks        | Non-blocking ops     | CompletableFuture, ForkJoinPool, Flow, Reactor  |
| coordination       | Thread coordination  | Latch, Barrier, Semaphore, StructuredTaskScope  |
| metrics-monitor    | Observability        | Atomic, Adder, ScheduledExecutorService         |
| application-runner | Bootstrap            | Spring Boot lifecycle, config injection         |

# Running the Project

## Prerequisites

- JDK 17 or JDK 21 (LTS)
- Maven 3.8+

## Running on Java 17

mvn clean install
mvn -pl application-runner spring-boot:run -Dspring.profiles.active=java17

## Running on Java 21

mvn clean install
mvn -pl application-runner spring-boot:run -Dspring.profiles.active=java21

# Learning Outcomes

1. Master all concurrency primitives, executors, and collections in Java 17.
2. Transition seamlessly to Structured Concurrency and Virtual Threads in Java 21.
3. Understand the trade-offs between blocking, non-blocking, and reactive models.
4. Apply real-world concurrency patterns to financial systems.
5. Learn to monitor, tune, and gracefully shut down concurrent systems.
