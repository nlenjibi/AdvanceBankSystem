package com.bank.system.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;


import static com.bank.system.utils.ConsoleUtil.*;

public class Transaction {
    private final String transactionId;
    private final String accountNumber;
    private final String type; // "DEPOSIT" or "WITHDRAWAL"
    private final double amount;
    private final double balanceAfter;
    private final String timestamp;
    private static final AtomicInteger TRANSACTION_COUNTER = new AtomicInteger(0);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

    public Transaction(String accountNumber, String type, double amount, double balanceAfter) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.transactionId = generateTransactionId();
        this.timestamp = getCurrentTimestamp();
    }

    private String generateTransactionId() {
        return String.format("TXN%03d", TRANSACTION_COUNTER.incrementAndGet());
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }

    // Method to display transaction details
    public void displayTransactionDetails(double previousBalance) {

        print("TRANSACTION CONFIRMATION");
        print(subSeparator(60));
        print("Transaction ID: " + transactionId);
        print("Account: " + accountNumber);
        print("Type: " + type);
        printf("Amount: $%,.2f%n", amount);
        printf("Previous Balance: $%,.2f%n", previousBalance);
        printf("NewBalance : $%,.2f%n", balanceAfter);
        print("Date/Time: " + timestamp);
        print(subSeparator(60));
    }

    // Getters
    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public static int getTransactionCounter() {
        return TRANSACTION_COUNTER.get();
    }


}