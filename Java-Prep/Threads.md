# 🧵 Java Threads — Complete Guide

A comprehensive reference covering Java threading from basic threads to modern virtual threads.

---

## Table of Contents

1. [What is a Thread?](#1-what-is-a-thread)
2. [Creating Threads](#2-creating-threads)
   - [Extending Thread Class](#21-extending-thread-class)
   - [Implementing Runnable](#22-implementing-runnable-interface)
   - [Using Callable & Future](#23-using-callable--future)
3. [Thread Lifecycle](#3-thread-lifecycle)
4. [Thread Methods](#4-important-thread-methods)
5. [Thread Synchronization](#5-thread-synchronization)
6. [ExecutorService](#6-executorservice)
   - [Thread Pool Types](#61-types-of-thread-pools)
   - [submit() vs execute()](#62-submit-vs-execute)
   - [Shutdown](#63-shutdown-vs-shutdownnow)
7. [Virtual Threads (Java 21)](#7-virtual-threads-java-21)
8. [Async Programming](#8-async-programming)
   - [CompletableFuture Basics](#81-completablefuture-basics)
   - [Chaining & Transforming](#82-chaining--transforming)
   - [Combining Futures](#83-combining-futures)
   - [Error Handling](#84-error-handling)
   - [Running Async with ExecutorService](#85-running-async-with-executorservice)
   - [CompletableFuture vs Future](#86-completablefuture-vs-future)
9. [Thread vs Runnable vs ExecutorService vs Virtual Threads](#9-comparison--when-to-use-what)
10. [Common Threading Issues](#10-common-threading-issues)

---

## 1. What is a Thread?

A **thread** is the smallest unit of execution within a program. Java allows multiple threads to run **concurrently**, enabling tasks like:
- Downloading a file while updating the UI
- Handling multiple HTTP requests simultaneously
- Running background jobs while the main app runs

Every Java program starts with at least one thread — the **main thread**.

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Running on: " + Thread.currentThread().getName()); // main
    }
}
```

---

## 2. Creating Threads

### 2.1 Extending Thread Class

```java
public class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Thread running: " + Thread.currentThread().getName());
    }

    public static void main(String[] args) {
        MyThread t = new MyThread();
        t.start(); // Creates a new thread and calls run()
    }
}
```

> ⚠️ Calling `t.run()` directly does **not** create a new thread — it runs on the calling thread like a normal method.

**Drawback:** Since Java doesn't support multiple class inheritance, extending `Thread` means you can't extend any other class.

---

### 2.2 Implementing Runnable Interface

```java
public class MyRunnable implements Runnable {
    @Override
    public void run() {
        System.out.println("Runnable running: " + Thread.currentThread().getName());
    }
}

public class Main {
    public static void main(String[] args) {
        // Traditional
        Thread t1 = new Thread(new MyRunnable());
        t1.start();

        // Lambda (preferred for simple tasks)
        Thread t2 = new Thread(() -> System.out.println("Lambda thread running"));
        t2.start();
    }
}
```

**Prefer `Runnable` over extending `Thread`** because:
- Your class can still extend another class
- Separates task logic from thread management
- Works seamlessly with `ExecutorService`

---

### 2.3 Using Callable & Future

`Callable` is like `Runnable` but can **return a result** and **throw checked exceptions**.

```java
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Callable<Integer> task = () -> {
            Thread.sleep(1000);
            return 42; // Returns a result
        };

        Future<Integer> future = executor.submit(task);

        System.out.println("Doing other work while task runs...");

        Integer result = future.get(); // Blocks until result is ready
        System.out.println("Result: " + result); // Result: 42

        executor.shutdown();
    }
}
```

### Runnable vs Callable

**Runnable**
- Does not return a value (`void`)
- Cannot throw checked exceptions
- Used with `Thread` and `ExecutorService`

**Callable\<T\>**
- Returns a value of type `T`
- Can throw checked exceptions
- Used with `ExecutorService` only

---

## 3. Thread Lifecycle

```
NEW → RUNNABLE → RUNNING → BLOCKED/WAITING/TIMED_WAITING → TERMINATED
```

**`NEW`** — Thread created but `start()` not yet called.

**`RUNNABLE`** — Ready to run or currently running.

**`BLOCKED`** — Waiting to acquire a monitor lock.

**`WAITING`** — Waiting indefinitely (e.g., `wait()`, `join()`).

**`TIMED_WAITING`** — Waiting for a specified time (`sleep()`, `wait(timeout)`).

**`TERMINATED`** — `run()` has finished execution.

```java
Thread t = new Thread(() -> {
    try { Thread.sleep(500); } catch (InterruptedException e) {}
});

System.out.println(t.getState()); // NEW
t.start();
System.out.println(t.getState()); // RUNNABLE or TIMED_WAITING
t.join();
System.out.println(t.getState()); // TERMINATED
```

---

## 4. Important Thread Methods

**`start()`** — Starts a new thread and invokes `run()`.

**`run()`** — Contains the task logic (don't call directly).

**`sleep(ms)`** — Pauses current thread for given milliseconds.

**`join()`** — Waits for this thread to finish before continuing.

**`interrupt()`** — Requests the thread to stop what it's doing.

**`isAlive()`** — Returns `true` if thread has started and not yet terminated.

**`getName()` / `setName()`** — Gets or sets the thread's name.

**`getPriority()` / `setPriority()`** — Gets or sets priority (1–10, default 5).

**`currentThread()`** — Returns reference to the currently executing thread.

```java
Thread t = new Thread(() -> {
    for (int i = 0; i < 5; i++) {
        System.out.println("Working... " + i);
        try { Thread.sleep(200); } catch (InterruptedException e) {
            System.out.println("Thread interrupted!");
            return;
        }
    }
});

t.setName("WorkerThread");
t.start();
t.join(); // Main thread waits for t to finish
System.out.println("Worker done. Main continues.");
```

---

## 5. Thread Synchronization

When multiple threads access **shared mutable data**, race conditions can occur. Use synchronization to prevent this.

### Problem — Race Condition

```java
class Counter {
    int count = 0;

    void increment() { count++; } // Not thread-safe!
}
```

### Fix 1 — `synchronized` method

```java
class Counter {
    int count = 0;

    synchronized void increment() { count++; } // Only one thread at a time
}
```

### Fix 2 — `synchronized` block (finer control)

```java
class Counter {
    int count = 0;
    private final Object lock = new Object();

    void increment() {
        synchronized (lock) {
            count++;
        }
    }
}
```

### Fix 3 — `AtomicInteger` (lock-free, fastest)

```java
import java.util.concurrent.atomic.AtomicInteger;

class Counter {
    AtomicInteger count = new AtomicInteger(0);

    void increment() { count.incrementAndGet(); }
}
```

> Use `AtomicInteger`, `AtomicLong`, `AtomicBoolean` from `java.util.concurrent.atomic` for simple counter/flag operations — they are lock-free and faster than `synchronized`.

---

## 6. ExecutorService

`ExecutorService` is a high-level thread management interface that provides **thread pooling**, **task scheduling**, and **graceful shutdown** — replacing the need to manually create and manage threads.

**Benefits:**
- **Thread Pooling** — reuses threads instead of creating new ones (expensive operation)
- **Task Scheduling** — schedule tasks with delay or at fixed rates
- **Better Resource Management** — manages thread lifecycle automatically
- **Graceful Shutdown** — `shutdown()` and `shutdownNow()` for clean termination

```java
import java.util.concurrent.*;

public class ExecutorExample {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        for (int i = 1; i <= 5; i++) {
            int taskId = i;
            executor.submit(() ->
                System.out.println("Task " + taskId + " running on: " + Thread.currentThread().getName())
            );
        }

        executor.shutdown(); // No new tasks accepted; existing tasks finish
    }
}
```

---

### 6.1 Types of Thread Pools

**`newFixedThreadPool(n)`** — Fixed n threads. Use for predictable, bounded workloads.

**`newSingleThreadExecutor()`** — 1 thread. Use for sequential task execution.

**`newCachedThreadPool()`** — Grows/shrinks dynamically. Use for many short-lived tasks.

**`newScheduledThreadPool(n)`** — Scheduled/periodic. Use for cron-like jobs, delayed tasks.

**`newVirtualThreadPerTaskExecutor()`** — Virtual thread per task (Java 21). Use for high-concurrency I/O tasks.

```java
// Scheduled task example
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

scheduler.scheduleAtFixedRate(() ->
    System.out.println("Heartbeat ping at: " + System.currentTimeMillis()),
    0,    // Initial delay
    5,    // Period
    TimeUnit.SECONDS
);
```

---

### 6.2 `submit()` vs `execute()`

**`execute(Runnable)`**
- Returns `void`
- Cannot get a result from the task
- Exceptions are thrown in the worker thread and are harder to handle

**`submit(Runnable/Callable)`**
- Returns a `Future<?>`
- Can retrieve the result using `Future.get()`
- Exceptions are captured in the `Future` and can be handled via `get()`

```java
Future<String> future = executor.submit(() -> {
    Thread.sleep(500);
    return "Done!";
});

String result = future.get(2, TimeUnit.SECONDS); // Timeout-safe get
System.out.println(result); // Done!
```

---

### 6.3 `shutdown()` vs `shutdownNow()`

```java
executor.shutdown();     // Waits for running tasks to complete, rejects new ones
executor.shutdownNow();  // Tries to stop all running tasks immediately (interrupts threads)

// Best practice — wait with timeout
executor.shutdown();
if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
    executor.shutdownNow();
}
```

---

## 7. Virtual Threads (Java 21)

**Virtual threads** are lightweight threads managed by the **JVM**, not the OS. They are designed for high-throughput **I/O-bound** applications (e.g., web servers handling thousands of concurrent requests).

**Platform Thread**
- Managed by the OS
- ~1MB memory per thread (stack)
- Max practical count: thousands
- Best for CPU-bound tasks
- Created with `new Thread(...)`

**Virtual Thread**
- Managed by the JVM
- ~few KB memory per thread
- Max practical count: millions
- Best for I/O-bound tasks
- Created with `Thread.ofVirtual()`

### Creating a Virtual Thread

```java
// Option 1 — Direct creation
Thread vThread = Thread.ofVirtual().start(() -> {
    System.out.println("Running in virtual thread: " + Thread.currentThread());
});

vThread.join();
```

### Using ExecutorService with Virtual Threads

```java
import java.util.concurrent.*;

public class VirtualThreadExample {
    public static void main(String[] args) throws InterruptedException {
        // Each submitted task gets its own virtual thread
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 1; i <= 10; i++) {
                int taskId = i;
                executor.submit(() -> {
                    System.out.println("Task " + taskId + " on: " + Thread.currentThread());
                    Thread.sleep(100); // Simulated I/O
                    return null;
                });
            }
        } // Auto-shutdown via try-with-resources
    }
}
```

### Virtual Threads — What to Avoid

```java
// ❌ Don't use virtual threads for CPU-bound tasks
// (e.g., heavy computation, image processing)
// Stick to platform threads / ForkJoinPool for those.

// ❌ Avoid synchronized blocks inside virtual threads
// Use ReentrantLock instead — synchronized pins the carrier thread
ReentrantLock lock = new ReentrantLock();
lock.lock();
try {
    // critical section
} finally {
    lock.unlock();
}
```

---

## 8. Async Programming

Java's primary tool for async programming is **`CompletableFuture`** (introduced in Java 8). It lets you write non-blocking pipelines — start a task, attach callbacks, chain transformations, and combine results — all without manually managing threads or blocking on `get()`.

> `Future` (Java 5) can retrieve a result but forces you to block with `get()` and cannot chain or combine. `CompletableFuture` solves all of that.

---

### 8.1 CompletableFuture Basics

There are three ways to start an async task:

**`runAsync(Runnable)`** — runs a task with no return value.

**`supplyAsync(Supplier<T>)`** — runs a task and returns a value of type `T`.

**`completedFuture(value)`** — wraps an already-known value (useful for testing/stubs).

```java
import java.util.concurrent.CompletableFuture;

// Fire-and-forget (no result)
CompletableFuture<Void> cf1 = CompletableFuture.runAsync(() -> {
    System.out.println("Running async: " + Thread.currentThread().getName());
});

// Async task that returns a value
CompletableFuture<String> cf2 = CompletableFuture.supplyAsync(() -> {
    return "Hello from async!";
});

System.out.println(cf2.get()); // Hello from async!
```

> By default, `runAsync` and `supplyAsync` use the **common `ForkJoinPool`**. Pass a custom `ExecutorService` as the second argument to control which thread pool is used (see [section 8.5](#85-running-async-with-executorservice)).

---

### 8.2 Chaining & Transforming

Once a `CompletableFuture` completes, you can immediately pipe the result into the next step. All chaining methods are **non-blocking** — they register a callback and return a new `CompletableFuture`.

**`thenApply(fn)`** — transform the result (like `map`). Runs on the same thread.

**`thenApplyAsync(fn)`** — same transformation but runs on a different thread.

**`thenAccept(consumer)`** — consume the result with no return value.

**`thenRun(runnable)`** — run something after completion, ignores the result entirely.

```java
CompletableFuture<String> pipeline = CompletableFuture
    .supplyAsync(() -> "hello")                        // Step 1: produce
    .thenApply(s -> s.toUpperCase())                   // Step 2: transform → "HELLO"
    .thenApply(s -> "Result: " + s)                    // Step 3: transform → "Result: HELLO"
    .thenApply(s -> {
        System.out.println(s);                         // Step 4: side effect
        return s;
    });

pipeline.thenAccept(System.out::println);              // Step 5: consume final result
pipeline.thenRun(() -> System.out.println("Done!"));   // Step 6: run after, no result
```

---

### 8.3 Combining Futures

**`thenCompose(fn)`** — chain two dependent futures (flat-map). Use when the next step itself returns a `CompletableFuture`.

**`thenCombine(other, fn)`** — combine two independent futures when both complete.

**`allOf(futures...)`** — wait for ALL futures to complete (returns `CompletableFuture<Void>`).

**`anyOf(futures...)`** — complete as soon as ANY one future completes.

```java
// thenCompose — sequential dependency (future returns a future)
CompletableFuture<String> result = CompletableFuture
    .supplyAsync(() -> "user-123")
    .thenCompose(userId -> fetchUserFromDb(userId)); // fetchUserFromDb returns CompletableFuture<String>

// thenCombine — two independent tasks merged
CompletableFuture<String> price  = CompletableFuture.supplyAsync(() -> "Price: $99");
CompletableFuture<String> stock  = CompletableFuture.supplyAsync(() -> "Stock: 5");
CompletableFuture<String> merged = price.thenCombine(stock, (p, s) -> p + " | " + s);
System.out.println(merged.get()); // Price: $99 | Stock: 5

// allOf — wait for all
CompletableFuture<Void> all = CompletableFuture.allOf(
    CompletableFuture.supplyAsync(() -> "Task A"),
    CompletableFuture.supplyAsync(() -> "Task B"),
    CompletableFuture.supplyAsync(() -> "Task C")
);
all.join(); // Blocks until all three finish
System.out.println("All tasks done!");

// anyOf — first to finish wins
CompletableFuture<Object> fastest = CompletableFuture.anyOf(
    CompletableFuture.supplyAsync(() -> { Thread.sleep(300); return "Slow"; }),
    CompletableFuture.supplyAsync(() -> { Thread.sleep(100); return "Fast"; })
);
System.out.println(fastest.get()); // Fast
```

> **`thenCompose` vs `thenApply`** — use `thenApply` when your function returns a plain value; use `thenCompose` when your function returns another `CompletableFuture` (avoids `CompletableFuture<CompletableFuture<T>>`).

---

### 8.4 Error Handling

**`exceptionally(fn)`** — catch an exception and return a fallback value. The pipeline continues normally after this.

**`handle(fn)`** — always runs (success or failure). Receives both the result and the exception (one will be `null`).

**`whenComplete(fn)`** — always runs like `handle`, but cannot change the result — used for side effects like logging.

```java
// exceptionally — catch and recover
CompletableFuture<String> safe = CompletableFuture
    .supplyAsync(() -> {
        if (true) throw new RuntimeException("Something went wrong!");
        return "OK";
    })
    .exceptionally(ex -> {
        System.out.println("Caught: " + ex.getMessage());
        return "Fallback value"; // pipeline continues with this
    });

System.out.println(safe.get()); // Fallback value

// handle — always called, decides what to return
CompletableFuture<String> handled = CompletableFuture
    .supplyAsync(() -> "Success!")
    .handle((result, ex) -> {
        if (ex != null) return "Error: " + ex.getMessage();
        return result.toUpperCase(); // SUCCESS!
    });

// whenComplete — logging / cleanup, cannot change result
CompletableFuture<String> logged = CompletableFuture
    .supplyAsync(() -> "Data")
    .whenComplete((result, ex) -> {
        if (ex != null) System.out.println("Failed: " + ex.getMessage());
        else System.out.println("Finished with: " + result);
    });
```

---

### 8.5 Running Async with ExecutorService

By default `supplyAsync` / `runAsync` use `ForkJoinPool.commonPool()`, which is shared across the whole JVM. For production code — especially web servers or database calls — always pass a dedicated executor.

```java
ExecutorService executor = Executors.newFixedThreadPool(4);

CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
    // Runs on the custom pool, not ForkJoinPool
    return "Result from custom pool: " + Thread.currentThread().getName();
}, executor);

// Chain steps can also target a specific executor
CompletableFuture<String> cf2 = cf
    .thenApplyAsync(s -> s.toUpperCase(), executor);  // explicit pool for this step
    .thenApplyAsync(s -> "[" + s + "]");              // falls back to ForkJoinPool

System.out.println(cf2.get());
executor.shutdown();
```

> With **virtual threads (Java 21)**, you can pass `Executors.newVirtualThreadPerTaskExecutor()` for high-concurrency I/O pipelines with minimal overhead.

---

### 8.6 CompletableFuture vs Future

**`Future` (Java 5)**
- Can check if done with `isDone()`
- Retrieve result with `get()` — **blocks the calling thread**
- Cannot chain or transform results
- Cannot attach callbacks
- No built-in way to handle exceptions in the pipeline
- Cannot be manually completed

**`CompletableFuture` (Java 8+)**
- All of the above plus non-blocking pipelines
- Chain steps with `thenApply`, `thenCompose`, etc.
- Combine multiple futures with `allOf`, `anyOf`, `thenCombine`
- Handle errors inline with `exceptionally`, `handle`, `whenComplete`
- Can be manually completed with `complete(value)` or `completeExceptionally(ex)`
- Works seamlessly with `ExecutorService` and virtual threads

```java
// Future — must block to get result
Future<String> future = executor.submit(() -> "result");
String val = future.get(); // ← blocks here

// CompletableFuture — non-blocking callback
CompletableFuture.supplyAsync(() -> "result")
    .thenAccept(val -> System.out.println("Got: " + val)); // ← never blocks
```

---

## 9. Comparison — When to Use What

**Simple, one-off background task** → `Thread` with `Runnable` / lambda

**Need result from async task** → `Callable` + `Future` via `ExecutorService`

**Non-blocking async pipeline with callbacks** → `CompletableFuture`

**Multiple async tasks that depend on each other** → `CompletableFuture.thenCompose()`

**Multiple independent async tasks, wait for all** → `CompletableFuture.allOf()`

**Race multiple tasks, take the fastest** → `CompletableFuture.anyOf()`

**Fixed number of concurrent tasks** → `Executors.newFixedThreadPool(n)`

**Many short-lived tasks** → `Executors.newCachedThreadPool()`

**Periodic / scheduled jobs** → `Executors.newScheduledThreadPool(n)`

**High-concurrency I/O (web servers, DB calls)** → Virtual Threads (Java 21)

**CPU-intensive parallel work** → `ForkJoinPool` / parallel streams

---

## 10. Common Threading Issues

### 🔴 Race Condition
Two threads read/write shared data simultaneously without synchronization.
**Fix:** Use `synchronized`, `Lock`, or atomic classes.

### 🔴 Deadlock
Two threads each hold a lock the other needs — both wait forever.

```java
// ❌ Deadlock example
synchronized (lockA) {
    synchronized (lockB) { /* Thread 1 */ }
}
synchronized (lockB) {
    synchronized (lockA) { /* Thread 2 — deadlock! */ }
}

// ✅ Fix — always acquire locks in the same order
synchronized (lockA) {
    synchronized (lockB) { /* Both threads */ }
}
```

### 🔴 Thread Starvation
Low-priority threads never get CPU time because high-priority threads always run.
**Fix:** Use fair locks — `new ReentrantLock(true)`.

### 🔴 Memory Visibility (`volatile`)
One thread updates a variable but another thread reads a stale cached value.

```java
// ❌ Without volatile — thread may read cached value
private boolean running = true;

// ✅ With volatile — always reads from main memory
private volatile boolean running = true;
```

> `volatile` guarantees **visibility** but not **atomicity**. For compound operations (`count++`), use `synchronized` or `AtomicInteger`.
