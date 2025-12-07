package com.example.challenge.assignment2;

import java.time.LocalDateTime;

/*
 POJO representing a single row from bankTransactionsDataset.csv
*/
public class BankTransaction {

    private final String transactionId;
    private final LocalDateTime transactionDate; // derived from Transaction_Date
    private final double transactionAmount;
    private final String transactionType;      // e.g. "Debit", "Credit"

    private final int customerAge;
    private final String customerGender;
    private final double customerIncome;
    private final double accountBalance;

    private final String category;             // e.g. Food, Transport, Entertainment etc.
    private final String merchantName;
    private final String paymentMethod;        // e.g. Credit Card, Debit Card, Cash, Transfer, Wire etc.
    private final String city;

    private final boolean fraudulent;          // derived from Fraud_Flag = "Yes"/"No"
    private final String transactionStatus;    // e.g. Success, Failed, Pending
    private final int loyaltyPointsEarned;
    private final boolean discountApplied;

    public BankTransaction(
            String transactionId,
            LocalDateTime transactionDate,
            double transactionAmount,
            String transactionType,
            int customerAge,
            String customerGender,
            double customerIncome,
            double accountBalance,
            String category,
            String merchantName,
            String paymentMethod,
            String city,
            boolean fraudulent,
            String transactionStatus,
            int loyaltyPointsEarned,
            boolean discountApplied
    ) {
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.transactionAmount = transactionAmount;
        this.transactionType = transactionType;
        this.customerAge = customerAge;
        this.customerGender = customerGender;
        this.customerIncome = customerIncome;
        this.accountBalance = accountBalance;
        this.category = category;
        this.merchantName = merchantName;
        this.paymentMethod = paymentMethod;
        this.city = city;
        this.fraudulent = fraudulent;
        this.transactionStatus = transactionStatus;
        this.loyaltyPointsEarned = loyaltyPointsEarned;
        this.discountApplied = discountApplied;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public double getTransactionAmount() {
        return transactionAmount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public int getCustomerAge() {
        return customerAge;
    }

    public String getCustomerGender() {
        return customerGender;
    }

    public double getCustomerIncome() {
        return customerIncome;
    }

    public double getAccountBalance() {
        return accountBalance;
    }

    public String getCategory() {
        return category;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getCity() {
        return city;
    }

    public boolean isFraudulent() {
        return fraudulent;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public int getLoyaltyPointsEarned() {
        return loyaltyPointsEarned;
    }

    public boolean isDiscountApplied() {
        return discountApplied;
    }

    @Override
    public String toString() {
        return "BankTransaction{" +
                "transactionId='" + transactionId + '\'' +
                ", transactionDate=" + transactionDate +
                ", transactionAmount=" + transactionAmount +
                ", transactionType='" + transactionType + '\'' +
                ", customerAge=" + customerAge +
                ", customerGender='" + customerGender + '\'' +
                ", customerIncome=" + customerIncome +
                ", accountBalance=" + accountBalance +
                ", category='" + category + '\'' +
                ", merchantName='" + merchantName + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", city='" + city + '\'' +
                ", fraudulent=" + fraudulent +
                ", transactionStatus='" + transactionStatus + '\'' +
                ", loyaltyPointsEarned=" + loyaltyPointsEarned +
                ", discountApplied=" + discountApplied +
                '}';
    }
}
