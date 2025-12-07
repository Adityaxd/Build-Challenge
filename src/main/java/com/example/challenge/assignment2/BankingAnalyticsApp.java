package com.example.challenge.assignment2;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class BankingAnalyticsApp {

    public static void main(String[] args) {
        Path csvPath = Path.of("data", "bankTransactionsDataset.csv");
        BankTransactionRepository repository = new BankTransactionRepository(csvPath);
        List<BankTransaction> transactions = repository.findAll();

        System.out.println("\nBanking Analytics Application Started");
        System.out.println("\nTotal transactions loaded : " + transactions.size());

        BankingAnalyticsService analyticsService = new BankingAnalyticsService(transactions);

        // Total by category
        System.out.println("\n**** Total Amount by Category ****");
        printMapSortedByValue(analyticsService.totalAmountByCategory());

        // Total by city
        System.out.println("\n**** Total Amount by City ****");
        printMapSortedByValue(analyticsService.totalAmountByCity());

        // Total by payment method
        System.out.println("\n**** Total Amount by Payment Method ****");
        printMapSortedByValue(analyticsService.totalAmountByPaymentMethod());

        // Total fraudulent amount
        System.out.println("\n**** Total Fraudulent Amount ****");
        System.out.printf("Fraudulent total: %.2f%n", analyticsService.totalFraudulentAmount());

        // Largest transaction
        System.out.println("\n**** Largest Transaction ****");
        BankTransaction largestTransaction = analyticsService.largestTransaction();
        if (largestTransaction != null) {
            System.out.println(largestTransaction);
        } else {
            System.out.println("No transactions found.");
        }

        // Top 5 merchants
        System.out.println("\n**** Top 5 Merchants by Total Amount ****");
        analyticsService.topMerchantsByTotalAmount(5)
                .forEach(entry -> System.out.printf("Merchant: %-25s Total Amount: %.2f%n",
                        entry.getKey(), entry.getValue()));

        // Average amount by age band
        System.out.println("\n**** Average Amount by Age Band ****");
        printMapSortedByValue(analyticsService.averageAmountByAgeBand());
    }

    // private static void printMap(Map<String, Double> map) {
    // map.forEach((key, value) -> System.out.printf("%s -> %.2f%n", key, value));
    // }

    private static void printMapSortedByValue(Map<String, Double> map) {
        map.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> System.out.printf("%-15s -> %10.2f%n", entry.getKey(), entry.getValue()));
    }
}