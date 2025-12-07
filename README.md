# Build Challenge – Producer/Consumer & Banking Analytics (Java)

This repo contains my solution for the Build Challenge in **Java 17**, split into:

- **Assignment 1** – Producer–Consumer with a custom bounded blocking queue using `wait` / `notifyAll`.
- **Assignment 2** – Banking transactions analytics over a CSV dataset using **Java Streams** and **lambda expressions**.

Everything is set up as a standard **Maven** project with **JUnit 5** tests.

---

## Tech Stack

- Java 17  
- Maven 3.9.11 
- JUnit 5 for tests  
- Apache Commons CSV for robust CSV parsing (`BankTransactionRepository`)  
- Core Java APIs for filesystem, dates, and data processing (`java.nio.file`, `java.time`, collections, `java.util.stream`)

---

## Project Structure

```text
Build-Challenge/
  data/
    bankTransactionsDataset.csv         # USA banking transactions dataset (2023–2024)

  src/
    main/
      java/
        com/example/challenge/
          assignment1/
            BoundedBlockingQueue.java       # Custom bounded blocking queue (wait/notifyAll)
            Producer.java                   # Producer thread (reads from source list)
            Consumer.java                   # Consumer thread (writes to destination list)
            ProducerConsumerDemo.java       # Demo app for Assignment 1

          assignment2/
            BankTransaction.java            # POJO representing one bank transaction row
            BankTransactionRepository.java  # CSV → List<BankTransaction>
            BankingAnalyticsService.java    # Stream-based analytics over the dataset
            BankingAnalyticsApp.java        # Console app for Assignment 2

    test/
      java/
        com/example/challenge/
          assignment1/
            ProducerConsumerTest.java       # Unit tests for bounded queue + producer/consumer
          assignment2/
            BankingAnalyticsServiceTest.java# Unit tests for analytics methods

  pom.xml
  README.md
````

---

## How to Build and Run

### Prerequisites

* Java 17 installed and on your `PATH`
* Maven installed (`mvn -v` should work)

From the project root:

### 1. Run all tests

```bash
mvn test
```

You should see something like:

* `ProducerConsumerTest` – 4 tests
* `BankingAnalyticsServiceTest` – 10 tests

All passing (14 tests total).

---

### 2. Build the JAR

```bash
mvn package
```

This will create:

```text
target/build-challenge-1.0-SNAPSHOT.jar
```

---

### 3. Run Assignment 1 demo (Producer–Consumer)

```bash
java -cp target/build-challenge-1.0-SNAPSHOT.jar \
  com.example.challenge.assignment1.ProducerConsumerDemo
```

Example output:

```text
Source data     : [1, 2, 3, 4, 5, 6, 7, 8, 9]
Destination data: [1, 2, 3, 4, 5, 6, 7, 8, 9]
```

This shows that every produced item went through the queue and was consumed in order.

---

### 4. Run Assignment 2 app (Banking Analytics)

```bash
java -cp target/build-challenge-1.0-SNAPSHOT.jar \
  com.example.challenge.assignment2.BankingAnalyticsApp
```

This:

* Loads `data/bankTransactionsDataset.csv`
* Runs several stream-based analytics
* Prints formatted summaries such as:

```text
Banking Analytics Application Started

Total transactions loaded : 5389

=== Total Amount by Category ===
Utilities       -> 1034864.54
Entertainment   -> 1018851.08
...

=== Total Amount by City ===
Chicago         -> 1463028.14
...

=== Total Amount by Payment Method ===
E-Wallet        -> 2784491.51
...

=== Total Fraudulent Amount ===
Fraudulent total: 6661043.01

=== Largest Transaction ===
BankTransaction{...}

=== Top 5 Merchants by Total Amount ===
Merchant: Smith LLC                 Total Amount: 35652.58
...

=== Average Amount by Age Band ===
50+             ->    2514.21
26-35           ->    2511.17
36-50           ->    2509.36
18-25           ->    2463.52
```

---

## Assignment 1 – Producer–Consumer with Bounded Blocking Queue

### Problem in my own words

Implement a classic producer–consumer pattern:

* One thread reads from a source container (I used a `List<Integer>`) and puts items into a shared bounded queue.
* Another thread reads from that queue and writes into a destination container.
* All of this must use proper thread synchronization, blocking behavior, and `wait/notify` – not busy loops.

### Design and Thought Process

#### 1. Custom queue vs built-ins

I did not use `ArrayBlockingQueue` / `BlockingQueue` from `java.util.concurrent`, because the point of the assignment is to show I can implement:

* A monitor (`synchronized` on a shared object)
* `wait()` when a condition (full/empty) is not satisfied
* `notifyAll()` when that condition changes

Core structure:

```java
public class BoundedBlockingQueue<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final int capacity;
    ...
}
```

#### 2. Capacity invariants

* Constructor rejects `capacity <= 0`.
* `put()` must block when `queue.size() == capacity`.
* `take()` must block when `queue.isEmpty()`.

#### 3. Synchronization strategy

* Use `synchronized (queue)` as the monitor.
* Both `put` and `take` use a `while` loop around `wait()` to guard against spurious wakeups.
* After modifying the queue, call `queue.notifyAll()` so that both producers and consumers get a chance to wake up.

```java
public void put(T element) throws InterruptedException {
    synchronized (queue) {
        while (queue.size() == capacity) {
            queue.wait();
        }
        queue.add(element);
        queue.notifyAll();
    }
}

public T take() throws InterruptedException {
    synchronized (queue) {
        while (queue.isEmpty()) {
            queue.wait();
        }
        T value = queue.remove();
        queue.notifyAll();
        return value;
    }
}
```

#### 4. Stopping the consumer (sentinel)

To stop the consumer cleanly:

* The producer, after sending all real values, sends one final `null` into the queue.
* The consumer treats `null` as “end of stream” and breaks out of its loop.

For a production system I would likely use a more explicit shutdown signal, but a sentinel `null` keeps the implementation straightforward for this exercise.

#### 5. Demo wiring

`ProducerConsumerDemo`:

* Creates a small `List<Integer>` as source.
* Creates an empty destination list.
* Uses a `BoundedBlockingQueue<Integer>` with a small capacity (e.g., 2) so we actually trigger blocking behavior.
* Spins up `Producer` and `Consumer` threads, joins them, and prints:

```text
Source data     : [...]
Destination data: [...]
```

This provides a quick visual sanity check.

---

### Assignment 1 – Tests

Tests live in `ProducerConsumerTest.java`. They cover:

1. **Constructor validation**

   ```java
   constructorRejectsNonPositiveCapacity()
   ```

   Ensures `IllegalArgumentException` is thrown for zero or negative capacity. This pins down the precondition for `BoundedBlockingQueue`.

2. **End-to-end correctness**

   ```java
   allItemsMoveFromSourceToDestination()
   ```

   * Uses a small source list `[10, 20, 30, 40, 50]`.
   * Starts real `Producer` and `Consumer` threads.
   * Joins them and asserts that the destination list exactly equals the source list, preserving order.

3. **`put()` blocking semantics**

   ```java
   putBlocksWhenQueueIsFullUntilSpaceIsFreed()
   ```

   * Creates a queue of capacity 1.
   * Fills it with `queue.put(1)`.
   * Starts a producer thread that tries to `queue.put(2)` (this should block).
   * Uses a brief `Thread.sleep` and asserts the producer is still alive (blocked).
   * Calls `queue.take()` to free space.
   * Asserts the producer finishes and that `2` is eventually read from the queue.

4. **`take()` blocking semantics**

   ```java
   takeBlocksWhenQueueIsEmptyUntilElementIsAvailable()
   ```

   * Starts a consumer thread calling `queue.take()` on an empty queue.
   * Asserts the consumer is initially blocked.
   * Puts `42` into the queue.
   * Joins the consumer and verifies it received exactly `42`.

Together, these tests show that:

* The queue enforces its capacity correctly.
* The producer/consumer wiring works.
* The `wait`/`notifyAll` logic behaves correctly under both full and empty conditions.

---

## Assignment 2 – Banking Transactions Analytics (Streams + CSV)

### Dataset Choice

For Assignment 2, I wanted something that actually feels like financial data that a company such as Intuit might deal with. I chose a:

> USA Banking Transactions Dataset (2023–2024)

Key properties:

* Around 5,000 rows → small enough to load into memory comfortably.
* Columns such as:

  * `Transaction_ID`, `Transaction_Date`, `Transaction_Amount`, `Transaction_Type`
  * `Customer_Age`, `Customer_Gender`, `Customer_Income`, `Account_Balance`
  * `Category`, `Merchant_Name`, `Payment_Method`, `City`
  * `Fraud_Flag` (Yes/No), `Transaction_Status`
  * `Loyalty_Points_Earned`, `Discount_Applied` (Yes/No)
* Saved as: `data/bankTransactionsDataset.csv`

This dataset is a good fit for aggregation and grouping operations and feels realistic for a financial analytics task.

---

### Data Model – `BankTransaction`

`BankTransaction` is a plain Java object representing one CSV row:

* `String transactionId`
* `LocalDateTime transactionDate`
* `double transactionAmount`
* `String transactionType`
* `int customerAge`
* `String customerGender`
* `double customerIncome`
* `double accountBalance`
* `String category`
* `String merchantName`
* `String paymentMethod`
* `String city`
* `boolean fraudulent`
* `String transactionStatus`
* `int loyaltyPointsEarned`
* `boolean discountApplied`

Flags like fraud and discount are mapped to booleans (`Yes`/`No` → `true`/`false`), which makes downstream filtering and aggregation straightforward.

Dates are parsed into `LocalDateTime` using a `DateTimeFormatter` that matches the dataset format.

---

### Loading the CSV – `BankTransactionRepository`

`BankTransactionRepository` is responsible for reading the CSV and mapping each row to a `BankTransaction`.

Key choices:

1. **Header-aware parsing with Apache Commons CSV**

   * Uses `CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true)...`.
   * This lets the code refer to columns by name (via constants like `COLUMN_TRANSACTION_ID`) rather than hard-coded indices.
   * Changes in column ordering do not break the parser as long as headers remain consistent.

2. **Type conversion**

   * `Transaction_Amount`, `Customer_Income`, `Account_Balance` → `double`
   * `Customer_Age`, `Loyalty_Points_Earned` → `int`
   * `Fraud_Flag`, `Discount_Applied` → booleans via `"Yes"/"No"` checks.

   Some numeric fields use helper methods like `parseDoubleOrDefault` / `parseIntOrDefault` to fall back to defaults when values are missing or malformed, so a bad value in a non-critical column does not kill the entire parse.

3. **Error behavior**

   * If a header name is missing and you try to access it, Commons CSV throws a clear `IllegalArgumentException`.
   * For the dataset used in this challenge, all required columns are present, so parsing completes cleanly.

`findAll()` returns a `List<BankTransaction>` built in a single pass. Given the dataset size, reading it fully into memory is perfectly reasonable. For very large datasets, a streaming approach or chunked processing would be more appropriate.

---

### Analytics Layer – `BankingAnalyticsService`

Once I have a `List<BankTransaction>`, I feed it into `BankingAnalyticsService`.

Important design choice:

```java
this.transactions = List.copyOf(transactions);
```

This creates a defensive copy so that:

* The service sees a stable view of the data.
* Callers cannot mutate the backing list and accidentally change analytics results.

All analytics methods rely on **Java Streams** and method references.

#### Implemented analytics

1. **Total amount by category**

   ```java
   Map<String, Double> totalAmountByCategory()
   ```

   ```java
   transactions.stream()
       .collect(Collectors.groupingBy(
           BankTransaction::getCategory,
           Collectors.summingDouble(BankTransaction::getTransactionAmount)
       ));
   ```

2. **Total amount by city**

   ```java
   Map<String, Double> totalAmountByCity()
   ```

   Same grouping pattern, but keyed by `BankTransaction::getCity`.

3. **Total amount by payment method**

   ```java
   Map<String, Double> totalAmountByPaymentMethod()
   ```

   Grouped on `getPaymentMethod()`.

4. **Total fraudulent amount**

   ```java
   double totalFraudulentAmount()
   ```

   Filters to `isFraudulent() == true` and sums `getTransactionAmount()`.

5. **Largest transaction**

   ```java
   BankTransaction largestTransaction()
   ```

   Uses `stream().max(Comparator.comparingDouble(BankTransaction::getTransactionAmount))`.
   Returns `null` if the dataset is empty.

6. **Average amount by age band**

   ```java
   Map<String, Double> averageAmountByAgeBand()
   ```

   * Maps raw ages into bands: `18-25`, `26-35`, `36-50`, `50+`, `Unknown` for invalid or out-of-range ages.
   * Groups by band and computes the average with `Collectors.averagingDouble`.

7. **Top N merchants by total amount**

   ```java
   List<Map.Entry<String, Double>> topMerchantsByTotalAmount(int n)
   ```

   * Groups by `merchantName` and sums `transactionAmount`.
   * Sorts in descending order of total amount.
   * Returns the first `n` entries (or the whole list if `n` exceeds the number of merchants).

---

### Console App – `BankingAnalyticsApp`

This class ties everything together:

1. Constructs a `Path` to `data/bankTransactionsDataset.csv`.
2. Uses `BankTransactionRepository` to load all transactions.
3. Instantiates `BankingAnalyticsService` with the loaded list.
4. Runs all analytics and prints:

   * Total amount by category (sorted by value descending)
   * Total amount by city
   * Total amount by payment method
   * Total fraudulent amount
   * Largest transaction
   * Top 5 merchants by total amount
   * Average amount by age band

Output formatting is done with `printf` and padded keys so the summaries are readable and aligned.

---

## Assignment 2 – Tests

All tests are in `BankingAnalyticsServiceTest.java` and use a helper:

```java
private BankTransaction tx(String id,
                           double amount,
                           String category,
                           String paymentMethod,
                           boolean fraudulent,
                           int age,
                           String city,
                           String merchantName)
```

This allows building small, focused in-memory datasets without reading the CSV during tests.

Covered scenarios:

1. **Totals by category**

   ```java
   totalAmountByCategory_sumsCorrectly()
   ```

   * Two “Food” transactions and one “Transport”.
   * Verifies Food = 150, Transport = 200, and that only these two keys are present.

2. **Totals by payment method**

   ```java
   totalAmountByPaymentMethod_sumsCorrectly()
   ```

   * Sums amounts by `"Credit Card"` and `"Debit Card"`.
   * Validates the correct totals and number of keys.

3. **Fraudulent amount (normal mix)**

   ```java
   totalFraudulentAmount_sumsOnlyFraudulentTransactions()
   ```

   * Mix of fraudulent and non-fraudulent rows.
   * Ensures only flagged transactions are added to the total.

4. **Fraudulent amount (no fraud)**

   ```java
   totalFraudulentAmount_isZeroWhenNoFraudulentTransactions()
   ```

   * No fraudulent transactions.
   * Asserts that the total fraudulent amount is exactly `0.0`.

5. **Largest transaction on non-empty list**

   ```java
   largestTransaction_returnsTransactionWithMaxAmount()
   ```

   * Three transactions with different amounts.
   * Ensures the transaction with the maximum amount is returned and the ID matches.

6. **Largest transaction on empty list**

   ```java
   largestTransaction_returnsNullForEmptyList()
   ```

   * No transactions in the input.
   * Expects `null` as the result.

7. **Average by age band (valid ages)**

   ```java
   averageAmountByAgeBand_groupsIntoCorrectBands()
   ```

   * Creates transactions in the `18-25`, `26-35`, and `50+` age bands.
   * Checks the computed averages per band.
   * Asserts that `"Unknown"` is not present in this scenario.

8. **Average by age band (unknown ages)**

   ```java
   averageAmountByAgeBand_putsInvalidAgesIntoUnknownBand()
   ```

   * Ages `0` and `-5` are treated as invalid and grouped under `"Unknown"`.
   * Age `30` falls into the `26-35` band.
   * Validates averages for `"Unknown"` and `"26-35"`.

9. **Top merchants sorted correctly**

   ```java
   topMerchantsByTotalAmount_returnsSortedDescending()
   ```

   * Sets up merchants where one clearly has the highest combined amount.
   * Checks that the list is ordered by total amount descending.

10. **Top merchants when N > merchant count**

    ```java
    topMerchantsByTotalAmount_handlesRequestLargerThanMerchantCount()
    ```

    * Only two merchants in the dataset, but the request asks for top 5.
    * Ensures no errors and verifies that the returned list size matches the number of actual merchants.

---

## Final Notes

* Assignment 1 focuses on low-level concurrency: implementing a bounded blocking queue, using `wait`/`notifyAll`, and coordinating producer and consumer threads correctly.
* Assignment 2 focuses on functional-style data processing in Java: Streams, lambdas, grouping, aggregation, and a clean domain model over a realistic CSV dataset.

The project is structured so that:

* Each assignment has its own package under `com.example.challenge`.
* Each major behavior has unit tests.
* `ProducerConsumerDemo` and `BankingAnalyticsApp` are runnable entry points that demonstrate the implementations end-to-end.

This README documents the structure, how to run the code, design choices, and test coverage so the reviewer can quickly understand how everything fits together.

## References
* USA Banking Transactions Dataset (2023-2024), Kaggle.
* Various open-source Java projects and documentation for stream and testing patterns.
* Stack Overflow discussions for clarifying Java stream and concurrency edge cases.
* General Java documentation and guides (Oracle Docs, official tutorials, etc.).

