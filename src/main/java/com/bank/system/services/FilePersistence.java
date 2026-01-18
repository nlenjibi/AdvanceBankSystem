package com.bank.system.services;

import com.bank.system.models.*;
import static com.bank.system.utils.ValidationUtils.*;
import static com.bank.system.utils.ConsoleUtil.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class FilePersistence {
    private static final String ACCOUNTS_FILE = "data/accounts.txt";
    private static final String TRANSACTIONS_FILE = "data/transactions.txt";
    private static final String DELIMITER = "|";
    private static final int ACCOUNT_FIELDS = 8;
    private static final int TRANSACTION_FIELDS = 6;

    /**
     * Saves accounts to the accounts file
     */
    public void saveAccounts(Map<String, Account> accounts) {
        Path path = Paths.get(ACCOUNTS_FILE);

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }else {
                print("Error creating directories for " + ACCOUNTS_FILE + " failed ");
            }
            List<Account> sorted = new ArrayList<>(accounts.values());
            sorted.sort(Comparator.comparing(Account::getAccountNumber));

            Set<String> written = new HashSet<>();
            for (Account account : sorted) {
                if (!written.add(account.getAccountNumber())) {
                    print("Skipped duplicate account during save: " + account.getAccountNumber());
                    continue;
                }
                writer.write(serializeAccount(account));
                writer.newLine();
            }

            print("Accounts saved to " + ACCOUNTS_FILE);
        } catch (IOException e) {
            print("Error saving accounts: " + e.getMessage());
        }
    }

    /**
     * Saves transactions to the transactions file
     */
   public void saveTransactions(List<Transaction> transactions) {
       Path path = Paths.get(TRANSACTIONS_FILE);

       try (BufferedWriter writer = Files.newBufferedWriter(path)) {
           if (path.getParent() != null) {
               Files.createDirectories(path.getParent());
           }else {
               print("Error creating directories for " + TRANSACTIONS_FILE + " failed" );
           }
           List<Transaction> sorted = new ArrayList<>(transactions);
           sorted.sort(Comparator.comparing(Transaction::getTransactionId));

           Set<String> seenIds = new HashSet<>();
           for (Transaction transaction : sorted) {
               if (!seenIds.add(transaction.getTransactionId())) {
                   print("Skipped duplicate transaction during save: " + transaction.getTransactionId());
                   continue;
               }
               writer.write(serializeTransaction(transaction));
               writer.newLine();
           }
           print("Transactions saved to " + TRANSACTIONS_FILE);
       } catch (IOException e) {
           print("Error saving transactions: " + e.getMessage());
       }
   }

    /**
     * Loads accounts from the accounts file
     */
    public Map<String, Account> loadAccounts() {
        return loadAccounts(new HashMap<>());
    }

    public Map<String, Account> loadAccounts(Map<String, Account> existingAccounts) {
        Map<String, Account> accounts = new HashMap<>(existingAccounts);
        Path path = Paths.get(ACCOUNTS_FILE);

        if (!Files.exists(path)) {
            if (existingAccounts.isEmpty()) {
                print("Accounts file does not exist. Starting with empty accounts.");
            } else {
                print("Accounts file does not exist. Retaining existing accounts.");
            }
            return accounts;
        }

        try {
            List<String> lines = Files.readAllLines(path);
            print("Loading account data from files...");

            Set<String> seenAccountNumbers = new HashSet<>(accounts.keySet());
            int loadedCount = 0;
            for (String line : lines) {
                Optional<Account> accountOpt = deserializeAccount(line);
                if (accountOpt.isEmpty()) {
                    continue;
                }
                Account account = accountOpt.get();
                if (!seenAccountNumbers.add(account.getAccountNumber())) {
                    print("Skipping duplicate account entry for " + account.getAccountNumber());
                    continue;
                }
                accounts.put(account.getAccountNumber(), account);
                loadedCount++;
            }

            print("✓ " + loadedCount + " accounts loaded successfully from " + ACCOUNTS_FILE);
        } catch (IOException e) {
            print("Error loading accounts: " + e.getMessage());
        } catch (Exception e) {
            print("Error processing account data: " + e.getMessage());
        }

        return accounts;
    }

    /**
     * Loads transactions from the transactions file
     */
    public List<Transaction> loadTransactions() {
        return loadTransactions(Collections.emptyList());
    }

    public List<Transaction> loadTransactions(List<Transaction> existingTransactions) {
        List<Transaction> transactions = existingTransactions == null
                ? new ArrayList<>()
                : new ArrayList<>(existingTransactions);
        Path path = Paths.get(TRANSACTIONS_FILE);

        if (!Files.exists(path)) {
            print("Transactions file does not exist. Starting with empty transactions.");
            return transactions;
        }

        try {
            List<String> lines = Files.readAllLines(path);

            Set<String> seenTransactionIds = new HashSet<>();
            for (Transaction tx : transactions) {
                seenTransactionIds.add(tx.getTransactionId());
            }

            int loadedCount = 0;
            for (String line : lines) {
                Optional<Transaction> transactionOpt = deserializeTransaction(line);
                if (transactionOpt.isEmpty()) {
                    continue;
                }
                Transaction transaction = transactionOpt.get();
                if (!seenTransactionIds.add(transaction.getTransactionId())) {
                    print("Skipping duplicate transaction entry for " + transaction.getTransactionId());
                    continue;
                }
                transactions.add(transaction);
                loadedCount++;
            }

            print("✓ " + loadedCount + " transactions loaded successfully from " + TRANSACTIONS_FILE);
        } catch (IOException e) {
            print("Error loading transactions: " + e.getMessage());
        } catch (Exception e) {
            print("Error processing transaction data: " + e.getMessage());
        }

        return transactions;
    }

    private String serializeAccount(Account account) {
        Customer customer = account.getCustomer();
        String customerType = (customer instanceof PremiumCustomer) ? "Premium" : "Regular";
        return String.join(DELIMITER,
                account.getAccountNumber(),
                String.valueOf(account.getBalance()),
                account.getAccountType(),
                customerType,
                customer.getName(),
                String.valueOf(customer.getAge()),
                customer.getContact(),
                customer.getAddress()
        );
    }

    private Optional<Account> deserializeAccount(String line) {
        String[] parts = line.split("\\Q" + DELIMITER + "\\E");
        if (parts.length != ACCOUNT_FIELDS) {
            print("Skipping malformed account line: " + line);
            return Optional.empty();
        }

        try {
            String accountNumber = parts[0];
            double balance = Double.parseDouble(parts[1]);
            String accountType = parts[2];
            String customerType = parts[3];
            String customerName = parts[4];
            int customerAge = Integer.parseInt(parts[5]);
            String customerPhone = parts[6];
            String customerAddress = parts[7];

            if (!validateAccountNumber(accountNumber)) {
                print("Invalid account number: " + accountNumber);
                return Optional.empty();
            }
            if (!validateAddress(customerAddress)) {
                print("Invalid Address: " + customerAddress);
                return Optional.empty();
            }
            if (!validateName(customerName)) {
                print("Invalid name: " + customerName);
                return Optional.empty();
            }
            if (!validateContactNumber(customerPhone)) {
                print("Invalid phone: " + customerPhone);
                return Optional.empty();
            }
            if (!validateAge(customerAge)) {
                print("Invalid age: " + customerAge);
                return Optional.empty();
            }

            Customer customer;
            if ("Regular".equals(customerType)) {
                customer = new RegularCustomer(customerName, customerAge, customerPhone, customerAddress);
            } else if ("Premium".equals(customerType)) {
                customer = new PremiumCustomer(customerName, customerAge, customerPhone, customerAddress);
            } else {
                print("Unknown customer type: " + customerType);
                return Optional.empty();
            }

            Account account;
            if ("Savings".equals(accountType)) {
                account = new SavingsAccount(accountNumber, customer, balance);
            } else if ("Checking".equals(accountType)) {
                account = new CheckingAccount(accountNumber, customer, balance);
            } else {
                print("Unknown account type: " + accountType);
                return Optional.empty();
            }

            return Optional.of(account);
        } catch (NumberFormatException e) {
            print("Number format error while parsing account line: " + line);
            return Optional.empty();
        }
    }

    private String serializeTransaction(Transaction transaction) {
        return String.join(DELIMITER,
                transaction.getTransactionId(),
                transaction.getAccountNumber(),
                transaction.getType(),
                String.valueOf(transaction.getAmount()),
                String.valueOf(transaction.getBalanceAfter()),
                transaction.getTimestamp()
        );
    }

    private Optional<Transaction> deserializeTransaction(String line) {
        String[] parts = line.split("\\Q" + DELIMITER + "\\E");
        if (parts.length != TRANSACTION_FIELDS) {
            print("Skipping malformed transaction line: " + line);
            return Optional.empty();
        }

        try {
            String transactionId = parts[0];
            String accountNumber = parts[1];
            String type = parts[2];
            double amount = Double.parseDouble(parts[3]);
            double balanceAfter = Double.parseDouble(parts[4]);
            String timestamp = parts[5];

            return Optional.of(new Transaction(transactionId, accountNumber, type, amount, balanceAfter, timestamp));
        } catch (NumberFormatException e) {
            print("Number format error while parsing transaction line: " + line);
            return Optional.empty();
        }
    }

    private String getAccountType(Account account) {
        if (account instanceof SavingsAccount) {
            return "Savings";
        } else if (account instanceof CheckingAccount) {
            return "Checking";
        }
        return "Unknown";
    }
}
