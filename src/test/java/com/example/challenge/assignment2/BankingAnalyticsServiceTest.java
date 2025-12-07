package com.example.challenge.assignment2;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Unit tests for BankingAnalyticsService.
 */

class BankingAnalyticsServiceTest {

    @Test
    void totalAmountByCategory_sumsCorrectly() {
        List<BankTransaction> txs = List.of(
                tx("T1", 100.0, "Food", "Credit Card", false, 24, "Seattle", "MerchantA"),
                tx("T2", 50.0, "Food", "Debit Card", true, 30, "Seattle", "MerchantB"),
                tx("T3", 200.0, "Transport", "Credit Card", false, 40, "LA", "MerchantC")
        );

        BankingAnalyticsService service = new BankingAnalyticsService(txs);

        Map<String, Double> result = service.totalAmountByCategory();

        assertEquals(150.0, result.get("Food"), 1e-6);
        assertEquals(200.0, result.get("Transport"), 1e-6);
        assertEquals(2, result.size());
    }

    @Test
    void totalAmountByPaymentMethod_sumsCorrectly() {
        List<BankTransaction> txs = List.of(
                tx("T1", 100.0, "Food", "Credit Card", false, 24, "Seattle", "MerchantA"),
                tx("T2", 50.0, "Food", "Debit Card", true, 30, "Seattle", "MerchantB"),
                tx("T3", 200.0, "Transport", "Credit Card", false, 40, "LA", "MerchantC")
        );

        BankingAnalyticsService service = new BankingAnalyticsService(txs);

        Map<String, Double> result = service.totalAmountByPaymentMethod();

        assertEquals(300.0, result.get("Credit Card"), 1e-6); // 100 + 200
        assertEquals(50.0, result.get("Debit Card"), 1e-6);
        assertEquals(2, result.size());
    }

    @Test
    void totalFraudulentAmount_sumsOnlyFraudulentTransactions() {
        List<BankTransaction> txs = List.of(
                tx("T1", 100.0, "Food", "Credit Card", false, 24, "Seattle", "MerchantA"),
                tx("T2", 60.0, "Food", "Credit Card", true, 30, "Seattle", "MerchantB"),
                tx("T3", 40.0, "Transport", "Debit Card", true, 40, "LA", "MerchantC")
        );

        BankingAnalyticsService service = new BankingAnalyticsService(txs);

        double fraudulentTotal = service.totalFraudulentAmount();

        // 60 + 40
        assertEquals(100.0, fraudulentTotal, 1e-6);
    }

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

    @Test
    void largestTransaction_returnsNullForEmptyList() {
        BankingAnalyticsService service = new BankingAnalyticsService(List.of());

        BankTransaction largest = service.largestTransaction();

        assertNull(largest);
    }

    @Test
    void averageAmountByAgeBand_groupsIntoCorrectBands() {
        List<BankTransaction> txs = List.of(
                tx("T1", 100.0, "Food", "Credit Card", false, 22, "Seattle", "MerchantA"), // 18-25
                tx("T2", 200.0, "Food", "Credit Card", false, 24, "Seattle", "MerchantB"), // 18-25
                tx("T3", 300.0, "Transport", "Debit Card", false, 32, "LA", "MerchantC"),  // 26-35
                tx("T4", 400.0, "Transport", "Debit Card", false, 55, "NYC", "MerchantD")  // 50+
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

    @Test
    void topMerchantsByTotalAmount_returnsSortedDescending() {
        List<BankTransaction> txs = List.of(
                tx("T1", 100.0, "Food", "Credit Card", false, 22, "Seattle", "MerchantA"),
                tx("T2", 150.0, "Food", "Debit Card", false, 24, "Seattle", "MerchantB"),
                tx("T3", 50.0,  "Food", "Credit Card", false, 32, "LA", "MerchantA"), // MerchantA total = 150
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

    // Missing fraudulent transactions test
    @Test
void totalFraudulentAmount_isZeroWhenNoFraudulentTransactions() {
    List<BankTransaction> txs = List.of(
            tx("T1", 100.0, "Food", "Credit Card", false, 24, "Seattle", "MerchantA"),
            tx("T2", 200.0, "Transport", "Debit Card", false, 30, "LA", "MerchantB")
    );

    BankingAnalyticsService service = new BankingAnalyticsService(txs);

    double fraudulentTotal = service.totalFraudulentAmount();

    assertEquals(0.0, fraudulentTotal, 1e-6);
}

// Top merchants test with n > number of merchants
@Test
void topMerchantsByTotalAmount_handlesRequestLargerThanMerchantCount() {
    List<BankTransaction> txs = List.of(
            tx("T1", 100.0, "Food", "Credit Card", false, 22, "Seattle", "MerchantA"),
            tx("T2", 150.0, "Food", "Debit Card", false, 24, "Seattle", "MerchantB")
    );

    BankingAnalyticsService service = new BankingAnalyticsService(txs);

    // Request more merchants than we have
    var top5 = service.topMerchantsByTotalAmount(5);

    assertEquals(2, top5.size());
    assertTrue(top5.stream().anyMatch(e -> e.getKey().equals("MerchantA")));
    assertTrue(top5.stream().anyMatch(e -> e.getKey().equals("MerchantB")));
}

// Average amount by age band with unknown ages
@Test
void averageAmountByAgeBand_putsInvalidAgesIntoUnknownBand() {
    List<BankTransaction> txs = List.of(
            tx("T1", 100.0, "Food", "Credit Card", false, 0, "Seattle", "MerchantA"),    // unknown
            tx("T2", 200.0, "Food", "Credit Card", false, -5, "Seattle", "MerchantB"),   // unknown
            tx("T3", 300.0, "Food", "Credit Card", false, 30, "Seattle", "MerchantC")    // 26-35
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
                "Debit",       // transactionType (not important for analytics here)
                age,
                "Male",        // customerGender
                50000.0,       // customerIncome
                1000.0,        // accountBalance
                category,
                merchantName,
                paymentMethod,
                city,
                fraudulent,
                "Success",     // transactionStatus
                0,             // loyaltyPointsEarned
                false          // discountApplied
        );
    }
}
