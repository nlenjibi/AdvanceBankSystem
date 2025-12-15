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
    private String timestamp;
    private static final AtomicInteger TRANSACTION_COUNTER = new AtomicInteger(0);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

    public Transaction(String accountNumber, String type, double amount, double balanceAfter) {
        this(generateTransactionId(), accountNumber, type, amount, balanceAfter, getCurrentTimestamp());
    }

    public Transaction(String transactionId, String accountNumber, String type, double amount, double balanceAfter, String timestamp) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = timestamp;
        syncTransactionCounter(transactionId);
    }

    private static String generateTransactionId() {
        return String.format("TXN%03d", TRANSACTION_COUNTER.incrementAndGet());
    }

    private static void syncTransactionCounter(String transactionId) {
        if (transactionId != null && transactionId.startsWith("TXN")) {
            try {
                int value = Integer.parseInt(transactionId.substring(3));
                TRANSACTION_COUNTER.updateAndGet(current -> Math.max(current, value));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private static String getCurrentTimestamp() {
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


    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }


}