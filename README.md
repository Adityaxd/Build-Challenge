# Build Challenge – Producer-Consumer & Sales Analytics (Java)

## Overview

This repository contains solutions for both required assignments from the Build Challenge:

1. **Producer-Consumer Pattern** using thread synchronization, a bounded blocking queue, and wait/notify.
2. **CSV Sales Data Analysis** using Java Streams, lambda expressions, and functional-style aggregation.

## Requirements

- Java 17+
- Maven 3.8+

## How to Build

```bash
mvn clean test
mvn package


#Assignment wise breakdown.

# 🧩 Build Challenge Assignment 1: Producer–Consumer Pattern

## 📄 Overview

This assignment focuses on implementing the **classic Producer–Consumer pattern** to demonstrate **thread synchronization** and **concurrent programming** proficiency.

The core requirement was to implement a custom, thread-safe, bounded, and blocking queue using the low-level **`wait()` and `notifyAll()`** mechanism instead of the built-in `BlockingQueue` utility.

---

## 🎯 High-Level Design

The solution is split into three core components, coordinating through a single, shared data structure:

1.  **`BoundedBlockingQueue<T>`**: The custom, thread-safe, blocking queue implementation.
2.  **`Producer`**: Reads data from a source container and writes it to the shared queue.
3.  **`Consumer`**: Reads data from the shared queue and writes it to a destination container.

### System Flow

1.  The **Producer** reads `Integer` values from an input `List<Integer>` (the source).
2.  It calls `put()` on the `BoundedBlockingQueue<Integer>`, blocking if the queue is full.
3.  The **Consumer** continuously calls `take()` on the queue, blocking if the queue is empty.
4.  Consumed values are written to an output `List<Integer>` (the destination).
5.  The Producer sends a **`null` sentinel value** (poison pill) to signal the end of the data stream.
6.  The Consumer stops processing when it receives the `null` sentinel.


---

## ⚙️ BoundedBlockingQueue Implementation

This class is the heart of the solution and handles all concurrency logic.

### 1. Data Structure Choice

* **Backing Store**: `Queue<T> queue = new LinkedList<>();`
    * A `LinkedList` was chosen to provide FIFO (First-In, First-Out) behavior, which is essential for preserving the order of the data stream.
* **Capacity**: The queue is initialized with a fixed, immutable capacity.

### 2. Synchronization Strategy

* All access to the internal `queue` is guarded by a single **intrinsic lock** (`synchronized (queue)`).
* The `queue` object itself is used as the **monitor** for `wait()` and `notifyAll()` calls.

### 3. Blocking Logic (`put` and `take`)

The `while` loop structure is crucial for safety and adherence to best practices in concurrency:

| Method | Blocking Condition | Unblock Action |
| :--- | :--- | :--- |
| **`put(T element)`** | **Queue is Full**: `while (queue.size() == capacity)` | `queue.wait()` until space is available. |
| **`take()`** | **Queue is Empty**: `while (queue.isEmpty())` | `queue.wait()` until data is available. |

* **Why `while` instead of `if`?**
    * It guards against **spurious wakeups** (threads waking without a true `notify()`).
    * It correctly handles **race conditions** where multiple waiting threads are notified simultaneously; each thread re-checks the condition (`full` or `empty`) before proceeding.
* **Notification**: After every successful `put()` or `take()`, **`notifyAll()`** is called to wake up all waiting Producer or Consumer threads, allowing them to re-evaluate their condition.

---

## 🧑‍💻 Producer and Consumer Logic

### **`Producer`**

* **Function**: Reads all elements from the `source` list.
* **Termination**: After putting the last real value, it explicitly calls `queue.put(null)` to insert the sentinel value.

### **`Consumer`**

* **Function**: Loops indefinitely, calling `queue.take()`.
* **Termination**: If the `take()` call returns `null` (the sentinel), the consumer breaks the loop cleanly.
* **Data Handling**: All non-null values are added to the `destination` list.

---

## ▶️ Execution and Verification (`ProducerConsumerDemo`)

A `main` method was used to verify the end-to-end correctness of the implementation.

### Setup

* **Source Data**: `[1, 2, 3, 4, 5, 6, 7, 8, 9]`
* **Queue Capacity**: `3` (Ensures maximum thread blocking/coordination for the demo)
* Two threads (Producer and Consumer) are started.

### Sample Output (Printed to Console)
