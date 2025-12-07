package com.example.challenge.assignment2;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Repository responsible for loading BankTransaction data from a CSV file.
 * Uses Apache Commons CSV to correctly handle quoted fields and commas.
 */
public class BankTransactionRepository {

    // Column name constants (must match the CSV header exactly)
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

    // Loads all bank transactions from the CSV file.
    public List<BankTransaction> findAll() {
        try (Reader reader = Files.newBufferedReader(csvPath);
             CSVParser parser = new CSVParser(reader,
                     CSVFormat.DEFAULT
                             .withFirstRecordAsHeader()
                             .withTrim()
                             .withIgnoreEmptyLines())) {

            List<BankTransaction> result = new ArrayList<>();

            for (CSVRecord record : parser) {
                result.add(toTransaction(record));
            }

            return result;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file: " + csvPath, e);
        }
    }
    // Converts a CSVRecord to a BankTransaction object.
    private BankTransaction toTransaction(CSVRecord record) {
        String id = record.get(COL_TRANSACTION_ID);

        String dateRaw = record.get(COL_TRANSACTION_DATE);
        LocalDateTime dateTime = LocalDateTime.parse(dateRaw, DATE_TIME_FORMATTER);

        double amount = parseDouble(record.get(COL_TRANSACTION_AMOUNT));
        String type = record.get(COL_TRANSACTION_TYPE);

        int age = parseIntOrDefault(record.get(COL_CUSTOMER_AGE), 0);
        String gender = record.get(COL_CUSTOMER_GENDER);
        double income = parseDoubleOrDefault(record.get(COL_CUSTOMER_INCOME), 0.0);
        double balance = parseDoubleOrDefault(record.get(COL_ACCOUNT_BALANCE), 0.0);

        String category = record.get(COL_CATEGORY);
        String merchant = record.get(COL_MERCHANT_NAME);
        String paymentMethod = record.get(COL_PAYMENT_METHOD);
        String city = record.get(COL_CITY);

        String fraudRaw = record.get(COL_FRAUD_FLAG);
        boolean fraudulent = fraudRaw != null && fraudRaw.trim().equalsIgnoreCase("yes");

        String status = record.get(COL_TRANSACTION_STATUS);

        int loyaltyPoints = parseIntOrDefault(record.get(COL_LOYALTY_POINTS), 0);

        String discountRaw = record.get(COL_DISCOUNT_APPLIED);
        boolean discountApplied = discountRaw != null && discountRaw.trim().equalsIgnoreCase("yes");

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

    private double parseDouble(String raw) {
        return Double.parseDouble(raw.trim());
    }
    // Parses a double value from a string, returning a default if parsing fails.
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
    // Parses an integer value from a string, returning a default if parsing fails. 
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
