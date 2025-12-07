package com.example.challenge.assignment2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Repository class to load bank transaction data from a CSV file.
 */
public class BankTransactionRepository {

    // Column name constants
    private static final String COL_TRANSACTION_ID = "Transaction_ID";
    private static final String COL_TRANSACTION_DATE = "Transaction_Date";
    private static final String COL_TRANSACTION_AMOUNT = "Transaction_Amount";
    private static final String COL_TRANSACTION_TYPE = "Transaction_Type";

    private static final String COL_CUSTOMER_AGE = "Customer_Age";
    private static final String COL_CUSTOMER_GENDER = "Customer_Gender";
    private static final String COL_CUSTOMER_INCOME = "Customer_Income";
    private static final String COL_ACCOUNT_BALANCE = "Account_Balance";

    private static final String COL_CATEGORY = "Category";
    private static final String COL_MERCHANT_NAME = "Merchant_Name";
    private static final String COL_PAYMENT_METHOD = "Payment_Method";
    private static final String COL_CITY = "City";

    private static final String COL_FRAUD_FLAG = "Fraud_Flag";
    private static final String COL_TRANSACTION_STATUS = "Transaction_Status";
    private static final String COL_LOYALTY_POINTS = "Loyalty_Points_Earned";
    private static final String COL_DISCOUNT_APPLIED = "Discount_Applied";

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path csvPath;

    public BankTransactionRepository(Path csvPath) {
        this.csvPath = Objects.requireNonNull(csvPath, "CSV path must not be null");
    }

    // Load all data from CSV file
    public List<BankTransaction> findAll() {
        try (Stream<String> lines = Files.lines(csvPath)) {
            List<String> allLines = lines
                    .filter(line -> !line.isBlank())
                    .collect(Collectors.toList());

            if (allLines.isEmpty()) {
                return List.of();
            }

            String headerLine = allLines.get(0);
            String[] headerColumns = headerLine.split(",", -1);
            Map<String, Integer> columnIndex = buildColumnIndex(headerColumns);

            return allLines.stream()
                    .skip(1) // Skip header line
                    .map(line -> toTransaction(line, columnIndex))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file: " + csvPath, e);
        }
    }

    // Build a map of column names to their indices
    private Map<String, Integer> buildColumnIndex(String[] headerColumns) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < headerColumns.length; i++) {
            index.put(headerColumns[i].trim(), i);
        }
        required(index, COL_TRANSACTION_ID);
        required(index, COL_TRANSACTION_DATE);
        required(index, COL_TRANSACTION_AMOUNT);
        required(index, COL_CATEGORY);
        required(index, COL_PAYMENT_METHOD);
        required(index, COL_CITY);
        return index;
    }

    private void required(Map<String, Integer> index, String columnName) {
        if (!index.containsKey(columnName)) {
            throw new IllegalArgumentException(
                    "Missing required column: " + columnName + " in CSV file: " + csvPath);
        }
    }


    // Convert a CSV line to a BankTransaction object
    private BankTransaction toTransaction(String line, Map<String, Integer> columnIndex) {
        String[] columns = line.split(",", -1);

        String id = value(columns, columnIndex, COL_TRANSACTION_ID);
        String dateRaw = value(columns, columnIndex, COL_TRANSACTION_DATE);
        LocalDateTime dateTime = LocalDateTime.parse(dateRaw, DATE_TIME_FORMATTER);

        double amount = parseDouble(value(columns, columnIndex, COL_TRANSACTION_AMOUNT));
        String type = value(columns, columnIndex, COL_TRANSACTION_TYPE);

        int age = parseIntOrDefault(value(columns, columnIndex, COL_CUSTOMER_AGE), 0);
        String gender = value(columns, columnIndex, COL_CUSTOMER_GENDER);
        double income = parseDoubleOrDefault(value(columns, columnIndex, COL_CUSTOMER_INCOME), 0.0);
        double balance = parseDoubleOrDefault(value(columns, columnIndex, COL_ACCOUNT_BALANCE), 0.0);

        String category = value(columns, columnIndex, COL_CATEGORY);
        String merchant = value(columns, columnIndex, COL_MERCHANT_NAME);
        String paymentMethod = value(columns, columnIndex, COL_PAYMENT_METHOD);
        String city = value(columns, columnIndex, COL_CITY);

        String fraudRaw = value(columns, columnIndex, COL_FRAUD_FLAG);
        boolean fraudulent = "yes".equalsIgnoreCase(fraudRaw.trim());

        String status = value(columns, columnIndex, COL_TRANSACTION_STATUS);

        int loyaltyPoints = parseIntOrDefault(
                value(columns, columnIndex, COL_LOYALTY_POINTS), 0);

        String discountRaw = value(columns, columnIndex, COL_DISCOUNT_APPLIED);
        boolean discountApplied = "yes".equalsIgnoreCase(discountRaw.trim());

        // Create and return the BankTransaction object
        return new BankTransaction(
                id,
                dateTime,
                amount,
                type,
                age,
                gender,
                income,
                balance,
                category,
                merchant,
                paymentMethod,
                city,
                fraudulent,
                status,
                loyaltyPoints,
                discountApplied
        );
    }


    // Helper methods to extract and parse values from columns
    // Extract value from columns based on column name
    private String value(String[] columns, Map<String, Integer> index, String colName) {
        Integer idx = index.get(colName);
        if (idx == null || idx < 0 || idx >= columns.length) {
            return "";
        }
        return columns[idx].trim();
    }

    // Parse double value from string
    private double parseDouble(String raw) {
        return Double.parseDouble(raw.trim());
    }

    // Parse double value with default
    private double parseDoubleOrDefault(String raw, double defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // Parse integer value with default
    private int parseIntOrDefault(String raw, int defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
