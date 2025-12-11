package com.bank.system.services;

import models.*;
import utils.ValidationUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FilePersistence {
    private static final String ACCOUNTS_FILE = "data/accounts.txt";
    private static final String TRANSACTIONS_FILE = "data/transactions.txt";
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Saves accounts to the accounts file
     */
    public void saveAccounts(Map<String, Account> accounts) {
        try {
            List<String> lines = new ArrayList<>();
            
            for (Account account : accounts.values()) {
                String line = String.format("%s|%s|%s|%s|%s|%s",
                    account.getAccountNumber(),
                    account.getBalance(),
                    getAccountType(account),
                    account.getCustomer().getName(),
                    account.getCustomer().getEmail(),
                    account.getCustomer().getPhoneNumber()
                );
                lines.add(line);
            }
            
            Path path = Paths.get(ACCOUNTS_FILE);
            Files.write(path, lines);
            System.out.println("Accounts saved to " + ACCOUNTS_FILE);
        } catch (IOException e) {
            System.err.println("Error saving accounts: " + e.getMessage());
        }
    }

    /**
     * Saves transactions to the transactions file
     */
    public void saveTransactions(List<Transaction> transactions) {
        try {
            List<String> lines = transactions.stream()
                .map(transaction -> String.format("%s|%s|%s|%s|%s|%s",
                    transaction.getTransactionId(),
                    transaction.getAccountNumber(),
                    transaction.getType(),
                    transaction.getAmount(),
                    transaction.getBalanceAfter(),
                    transaction.getTimestamp().format(DATE_FORMAT)
                ))
                .collect(Collectors.toList());
            
            Path path = Paths.get(TRANSACTIONS_FILE);
            Files.write(path, lines);
            System.out.println("Transactions saved to " + TRANSACTIONS_FILE);
        } catch (IOException e) {
            System.err.println("Error saving transactions: " + e.getMessage());
        }
    }

    /**
     * Loads accounts from the accounts file
     */
    public Map<String, Account> loadAccounts() {
        Map<String, Account> accounts = new HashMap<>();
        
        try {
            Path path = Paths.get(ACCOUNTS_FILE);
            if (!Files.exists(path)) {
                System.out.println("Accounts file does not exist. Starting with empty accounts.");
                return accounts;
            }
            
            List<String> lines = Files.readAllLines(path);
            System.out.println("Loading account data from files...");
            
            for (String line : lines) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6) {
                    String accountNumber = parts[0];
                    double balance = Double.parseDouble(parts[1]);
                    String accountType = parts[2];
                    String customerName = parts[3];
                    String customerEmail = parts[4];
                    String customerPhone = parts[5];
                    
                    // Validate data before creating objects
                    if (!ValidationUtils.validateAccountNumber(accountNumber)) {
                        System.err.println("Invalid account number: " + accountNumber);
                        continue;
                    }
                    
                    if (!ValidationUtils.validateEmail(customerEmail)) {
                        System.err.println("Invalid email: " + customerEmail);
                        continue;
                    }
                    
                    if (!ValidationUtils.validateName(customerName)) {
                        System.err.println("Invalid name: " + customerName);
                        continue;
                    }
                    
                    if (!ValidationUtils.validatePhone(customerPhone)) {
                        System.err.println("Invalid phone: " + customerPhone);
                        continue;
                    }
                    
                    Customer customer = new RegularCustomer(customerName, customerEmail, customerPhone);
                    
                    Account account;
                    if ("Savings".equals(accountType)) {
                        account = new SavingsAccount(accountNumber, balance, customer);
                    } else if ("Checking".equals(accountType)) {
                        account = new CheckingAccount(accountNumber, balance, customer);
                    } else {
                        System.err.println("Unknown account type: " + accountType);
                        continue;
                    }
                    
                    accounts.put(accountNumber, account);
                }
            }
            
            System.out.println("✓ " + accounts.size() + " accounts loaded successfully from " + ACCOUNTS_FILE);
        } catch (IOException e) {
            System.err.println("Error loading accounts: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error processing account data: " + e.getMessage());
        }
        
        return accounts;
    }

    /**
     * Loads transactions from the transactions file
     */
    public List<Transaction> loadTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        
        try {
            Path path = Paths.get(TRANSACTIONS_FILE);
            if (!Files.exists(path)) {
                System.out.println("Transactions file does not exist. Starting with empty transactions.");
                return transactions;
            }
            
            List<String> lines = Files.readAllLines(path);
            
            for (String line : lines) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6) {
                    String transactionId = parts[0];
                    String accountNumber = parts[1];
                    String type = parts[2];
                    double amount = Double.parseDouble(parts[3]);
                    double balanceAfter = Double.parseDouble(parts[4]);
                    LocalDateTime timestamp = LocalDateTime.parse(parts[5], DATE_FORMAT);
                    
                    Transaction transaction = new Transaction(transactionId, accountNumber, type, amount, balanceAfter);
                    transaction.setTimestamp(timestamp);
                    transactions.add(transaction);
                }
            }
            
            System.out.println("✓ " + transactions.size() + " transactions loaded from " + TRANSACTIONS_FILE);
        } catch (IOException e) {
            System.err.println("Error loading transactions: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error processing transaction data: " + e.getMessage());
        }
        
        return transactions;
    }
    
    /**
     * Helper method to get the account type as a string
     */
    private String getAccountType(Account account) {
        if (account instanceof SavingsAccount) {
            return "Savings";
        } else if (account instanceof CheckingAccount) {
            return "Checking";
        }
        return "Unknown";
    }
}