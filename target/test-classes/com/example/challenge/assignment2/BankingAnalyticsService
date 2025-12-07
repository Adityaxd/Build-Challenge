package com.example.challenge.assignment2;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * Service that exposes various analytics over a list of BankTransaction
 * using Java Streams and lambdas.
 */
public class BankingAnalyticsService {

    private final List<BankTransaction> transactions;

    public BankingAnalyticsService(List<BankTransaction> transactions) {
        this.transactions = List.copyOf(transactions);
    }

  // Total transaction amount grouped by category.
    public Map<String, Double> totalAmountByCategory() {
        return transactions.stream()
                .collect(groupingBy(
                        BankTransaction::getCategory,
                        summingDouble(BankTransaction::getTransactionAmount)
                ));
    }

    // Total transaction amount grouped by category.
    public Map<String, Double> totalAmountByCity() {
        return transactions.stream()
                .collect(groupingBy(
                        BankTransaction::getCity,
                        summingDouble(BankTransaction::getTransactionAmount)
                ));
    }

    // Total transaction amount grouped by payment method.
    public Map<String, Double> totalAmountByPaymentMethod() {
        return transactions.stream()
                .collect(groupingBy(
                        BankTransaction::getPaymentMethod,
                        summingDouble(BankTransaction::getTransactionAmount)
                ));
    }

    // Total amount of fraudulent transactions.
    public double totalFraudulentAmount() {
        return transactions.stream()
                .filter(BankTransaction::isFraudulent)
                .mapToDouble(BankTransaction::getTransactionAmount)
                .sum();
    }

    // Largest single transaction.
    public BankTransaction largestTransaction() {
        return transactions.stream()
                .max(Comparator.comparingDouble(BankTransaction::getTransactionAmount))
                .orElse(null);
    }

    // Top N merchants by total transaction amount.
    public List<Map.Entry<String, Double>> topMerchantsByTotalAmount(int limit) {
        Map<String, Double> totalByMerchant = transactions.stream()
                .collect(groupingBy(
                        BankTransaction::getMerchantName,
                        summingDouble(BankTransaction::getTransactionAmount)
                ));

        return totalByMerchant.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Average transaction amount by customer age band.
    public Map<String, Double> averageAmountByAgeBand() {
        return transactions.stream()
                .collect(groupingBy(
                        tx -> toAgeBand(tx.getCustomerAge()),
                        averagingDouble(BankTransaction::getTransactionAmount)
                ));
    }

    private String toAgeBand(int age) {
        if (age <= 0) {
            return "Unknown";
        } else if (age <= 25) {
            return "18-25";
        } else if (age <= 35) {
            return "26-35";
        } else if (age <= 50) {
            return "36-50";
        } else {
            return "50+";
        }
    }
}
