package com.example.challenge.assignment2;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the banking analytics layer (Assignment 2).
 *
 * This suite verifies:
 * 1. Aggregation of total transaction amounts by category and payment method
 * 2. Computation of total fraudulent amount, including the zero-fraud case
 * 3. Selection of the largest transaction, and null behavior for an empty
 * dataset
 * 4. Grouping and averaging of transaction amounts by customer age band
 * 5. Ranking of top merchants by total transaction amount, including the case
 * where the requested top-N exceeds the number of available merchants
 * 6. Handling of invalid or out-of-range ages by placing them in an "Unknown"
 * band
 *
 * The tests use a helper method to quickly create BankTransaction instances
 * with specified attributes relevant to each test case.
 */

class BankingAnalyticsServiceTest {

    // Verifies that totalAmountByCategory correctly sums amounts per category.
    @Test
    void totalAmountByCategory_sumsCorrectly() {
        List<BankTransaction> txs = List.of(
                tx("T1", 100.0, "Food", "Credit Card", false, 24, "Seattle", "MerchantA"),
                tx("T2", 50.0, "Food", "Debit Card", true, 30, "Seattle", "MerchantB"),
                tx("T3", 200.0, "Transport", "Credit Card", false, 40, "LA", "MerchantC"));

        BankingAnalyticsService service = new BankingAnalyticsService(txs);

        Map<String, Double> result = service.totalAmountByCategory();

        assertEquals(150.0, result.get("Food"), 1e-6);
        assertEquals(200.0, result.get("Transport"), 1e-6);
        assertEquals(2, result.size());
    }

    // Verifies that totalAmountByPaymentMethod correctly sums amounts per payment
    // method.
    @Test
    void totalAmountByPaymentMethod_sumsCorrectly() {
        List<BankTransaction> txs = List.of(
                tx("T1", 100.0, "Food", "Credit Card", false, 24, "Seattle", "MerchantA"),
                tx("T2", 50.0, "Food", "Debit Card", true, 30, "Seattle", "MerchantB"),
                tx("T3", 200.0, "Transport", "Credit Card", false, 40, "LA", "MerchantC"));

        BankingAnalyticsService service = new BankingAnalyticsService(txs);

        Map<String, Double> result = service.totalAmountByPaymentMethod();

        assertEquals(300.0, result.get("Credit Card"), 1e-6); // 100 + 200
        assertEquals(50.0, result.get("Debit Card"), 1e-6);
        assertEquals(2, result.size());
    }

    // Ensures totalFraudulentAmount only includes transactions marked as
    // fraudulent.
    @Test
    void totalFraudulentAmount_sumsOnlyFraudulentTransactions() {
        List<BankTransaction> txs = List.of(
                tx("T1", 100.0, "Food", "Credit Card", false, 24, "Seattle", "MerchantA"),
                tx("T2", 60.0, "Food", "Credit Card", true, 30, "Seattle", "MerchantB"),
                tx("T3", 40.0, "Transport", "Debit Card", true, 40, "LA", "MerchantC"));

        BankingAnalyticsService service = new BankingAnalyticsService(txs);

        double fraudulentTotal = service.totalFraudulentAmount();

        // 60 + 40
        assertEquals(100.0, fraudulentTotal, 1e-6);
    }

    // Makes sure largestTransaction returns the transaction with the maximum
    // amount.
    @Test
    void largestTransaction_returnsTransactionWithMaxAmount() {
        BankTransaction t1 = tx("T1", 100.0, "Food", "Credit Card", false, 24, "Seattle", "MerchantA");
        BankTransaction t2 = tx("T2", 250.0, "Food", "Credit Card", false, 30, "Seattle", "MerchantB");
        BankTransaction t3 = tx("T3", 180.0, "Transport", "Debit Card", false, 40, "LA", "MerchantC");

        BankingAnalyticsService service = new BankingAnalyticsService(List.of(t1, t2, t3));

        BankTransaction largest = service.largestTransaction();

        assertNotNull(largest);
        assertEquals("T2", largest.getTransactionId());
        assertEquals(250.0, largest.getTransactionAmount(), 1e-6);
    }

    // Ensures largestTransaction returns null when there are no transactions.
    @Test
    void largestTransaction_returnsNullForEmptyList() {
        BankingAnalyticsService service = new BankingAnalyticsService(List.of());

        BankTransaction largest = service.largestTransaction();

        assertNull(largest);
    }

    // Verifies that averageAmountByAgeBand groups amounts into the correct age
    // bands.
    @Test
    void averageAmountByAgeBand_groupsIntoCorrectBands() {
        List<BankTransaction> txs = List.of(
                tx("T1", 100.0, "Food", "Credit Card", false, 22, "Seattle", "MerchantA"), // 18-25
                tx("T2", 200.0, "Food", "Credit Card", false, 24, "Seattle", "MerchantB"), // 18-25
                tx("T3", 300.0, "Transport", "Debit Card", false, 32, "LA", "MerchantC"), // 26-35
                tx("T4", 400.0, "Transport", "Debit Card", false, 55, "NYC", "MerchantD") // 50+
        );

        BankingAnalyticsService service = new BankingAnalyticsService(txs);

        Map<String, Double> result = service.averageAmountByAgeBand();

        // 18-25 -> (100 + 200) / 2 = 150
        assertEquals(150.0, result.get("18-25"), 1e-6);
        // 26-35 -> 300
        assertEquals(300.0, result.get("26-35"), 1e-6);
        // 50+ -> 400
        assertEquals(400.0, result.get("50+"), 1e-6);
        // no "Unknown" here
        assertFalse(result.containsKey("Unknown"));
    }

    // Verifies that topMerchantsByTotalAmount returns merchants sorted by total
    // amount descending.
    @Test
    void topMerchantsByTotalAmount_returnsSortedDescending() {
        List<BankTransaction> txs = List.of(
                tx("T1", 100.0, "Food", "Credit Card", false, 22, "Seattle", "MerchantA"),
                tx("T2", 150.0, "Food", "Debit Card", false, 24, "Seattle", "MerchantB"),
                tx("T3", 50.0, "Food", "Credit Card", false, 32, "LA", "MerchantA"), // MerchantA total = 150
                tx("T4", 500.0, "Transport", "Debit Card", false, 40, "NYC", "MerchantC") // MerchantC total = 500
        );

        BankingAnalyticsService service = new BankingAnalyticsService(txs);

        var top2 = service.topMerchantsByTotalAmount(2);

        assertEquals(2, top2.size());

        // First: MerchantC -> 500
        assertEquals("MerchantC", top2.get(0).getKey());
        assertEquals(500.0, top2.get(0).getValue(), 1e-6);

        // Second: MerchantA -> 150
        assertEquals("MerchantA", top2.get(1).getKey());
        assertEquals(150.0, top2.get(1).getValue(), 1e-6);
    }

    // Ensures totalFraudulentAmount is zero when there are no fraudulent
    // transactions.
    @Test
    void totalFraudulentAmount_isZeroWhenNoFraudulentTransactions() {
        List<BankTransaction> txs = List.of(
                tx("T1", 100.0, "Food", "Credit Card", false, 24, "Seattle", "MerchantA"),
                tx("T2", 200.0, "Transport", "Debit Card", false, 30, "LA", "MerchantB"));

        BankingAnalyticsService service = new BankingAnalyticsService(txs);

        double fraudulentTotal = service.totalFraudulentAmount();

        assertEquals(0.0, fraudulentTotal, 1e-6);
    }

    // Ensures topMerchantsByTotalAmount handles a requested N larger than the
    // merchant count.
    @Test
    void topMerchantsByTotalAmount_handlesRequestLargerThanMerchantCount() {
        List<BankTransaction> txs = List.of(
                tx("T1", 100.0, "Food", "Credit Card", false, 22, "Seattle", "MerchantA"),
                tx("T2", 150.0, "Food", "Debit Card", false, 24, "Seattle", "MerchantB"));

        BankingAnalyticsService service = new BankingAnalyticsService(txs);

        // Request more merchants than we have
        var top5 = service.topMerchantsByTotalAmount(5);

        assertEquals(2, top5.size());
        assertTrue(top5.stream().anyMatch(e -> e.getKey().equals("MerchantA")));
        assertTrue(top5.stream().anyMatch(e -> e.getKey().equals("MerchantB")));
    }

    // Verifies that invalid or out-of-range ages are grouped into the 'Unknown' age
    // band.
    @Test
    void averageAmountByAgeBand_putsInvalidAgesIntoUnknownBand() {
        List<BankTransaction> txs = List.of(
                tx("T1", 100.0, "Food", "Credit Card", false, 0, "Seattle", "MerchantA"), // unknown
                tx("T2", 200.0, "Food", "Credit Card", false, -5, "Seattle", "MerchantB"), // unknown
                tx("T3", 300.0, "Food", "Credit Card", false, 30, "Seattle", "MerchantC") // 26-35
        );

        BankingAnalyticsService service = new BankingAnalyticsService(txs);

        Map<String, Double> result = service.averageAmountByAgeBand();

        // Unknown band should be (100 + 200) / 2 = 150
        assertEquals(150.0, result.get("Unknown"), 1e-6);
        assertEquals(300.0, result.get("26-35"), 1e-6);
    }

    // Helper to construct BankTransaction objects quickly for tests
    private BankTransaction tx(String id,
            double amount,
            String category,
            String paymentMethod,
            boolean fraudulent,
            int age,
            String city,
            String merchantName) {
        LocalDateTime now = LocalDateTime.of(2023, 1, 1, 0, 0, 0);

        return new BankTransaction(
                id,
                now,
                amount,
                "Debit", // transactionType (not important for analytics here)
                age,
                "Male", // customerGender
                50000.0, // customerIncome
                1000.0, // accountBalance
                category,
                merchantName,
                paymentMethod,
                city,
                fraudulent,
                "Success", // transactionStatus
                0, // loyaltyPointsEarned
                false // discountApplied
        );
    }
}
