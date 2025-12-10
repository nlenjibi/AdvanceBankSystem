package com.bank.system.services;

import com.bank.system.enums.TransactionType;
import com.bank.system.exceptions.*;
import com.bank.system.models.Account;
import com.bank.system.models.Transaction;

import java.util.ArrayList;
import java.util.List;


public class TransactionManager {
    private final List<Transaction> allTransactions;

    private final AccountManager accountManager;
    private int transactionCount;

    public TransactionManager(AccountManager accountManager) {
        this.accountManager = accountManager;
        this.allTransactions = new ArrayList<>();

    }

    // Method to add a transaction
    public boolean addTransaction(Transaction transaction) {
        if (transaction == null) {
            return false;
        }
        allTransactions.add(transaction);
        return true;
    }

    // Method to calculate total withdrawals for an account

    public double totalWithdrawals(String accountNumber) {
        return sumAmountsForTypes(accountNumber, TransactionType.WITHDRAWAL);
    }
    public double totalTransfered(String accountNumber) {
        return sumAmountsForTypes(accountNumber, TransactionType.TRANSFER);
    }
    public double totalDeposits(String accountNumber) {
        return sumAmountsForTypes(accountNumber, TransactionType.DEPOSIT);
    }
    public double totalReceived(String accountNumber) {
        return sumAmountsForTypes(accountNumber, TransactionType.RECEIVE);
    }


    // Method to get the number of transactions
    public int getTransactionCount() {
        return transactionCount;
    }


    public boolean deposit(String accountNumber, double amount) throws InvalidAmountException {
        Account account = accountManager.getAccount(accountNumber);
        if (account == null) {
            throw new InvalidAmountException("Account not found: " + accountNumber);
        }

        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than 0");
        }

        boolean success = account.processTransaction(amount, TransactionType.DEPOSIT);
        double newBalance = account.getBalance();

        // Create and record the transaction
        if(success) {
            Transaction transaction = createTransaction(accountNumber, TransactionType.DEPOSIT, amount, newBalance);
            allTransactions.add(transaction);
            account.addTransaction(transaction);
            return true;
        }
        return false;

    }

    public boolean withdraw(String accountNumber, double amount) throws InvalidAmountException {
        Account account = accountManager.getAccount(accountNumber);
        if (account == null) {
            throw new InvalidAmountException("Account not found: " + accountNumber);
        }

        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than 0");
        }

        boolean success = account.processTransaction(amount, TransactionType.WITHDRAWAL);
        double newBalance = account.getBalance();

        if (success) {
            // Create and record the transaction

            Transaction transaction = createTransaction(accountNumber, TransactionType.WITHDRAWAL, amount, newBalance);
            allTransactions.add(transaction);
            account.addTransaction(transaction);
            return true;
        }
        return false;

    }

    public boolean transfer(String fromAccountNumber, String toAccountNumber, double amount)
            throws InvalidAmountException {
        Account fromAccount = accountManager.getAccount(fromAccountNumber);
        Account toAccount = accountManager.getAccount(toAccountNumber);

        if (fromAccount == null) {
            throw new InvalidAmountException("Source account not found: " + fromAccountNumber);
        }

        if (toAccount == null) {
            throw new InvalidAmountException("Destination account not found: " + toAccountNumber);
        }

        if (amount <= 0) {
            throw new InvalidAmountException("Transfer amount must be greater than 0");
        }

        // Perform withdrawal from source account
        boolean fromSuccess =  fromAccount.processTransaction(amount, TransactionType.WITHDRAWAL);
        double newFromBalance = fromAccount.getBalance();

        // Perform deposit to destination account

        boolean toSuccess = toAccount.processTransaction(amount, TransactionType.DEPOSIT);
        double newToBalance = toAccount.getBalance();

        // Record withdrawal transaction
        if(fromSuccess) {
            Transaction withdrawalTransaction = createTransaction(fromAccountNumber, TransactionType.TRANSFER, amount, newFromBalance);
            allTransactions.add(withdrawalTransaction);
            fromAccount.addTransaction(withdrawalTransaction);
        }
        // Record deposit transaction
        if (toSuccess) {
            Transaction depositTransaction = createTransaction(toAccountNumber, TransactionType.RECEIVE, amount, newToBalance);
            allTransactions.add(depositTransaction);
            toAccount.addTransaction(depositTransaction);
        }
        return true;
    }
    public List<Transaction> getTransactionsForAccount(String accountNumber) {
        List<Transaction> accountTransactions = new ArrayList<>();
        for (Transaction transaction : allTransactions) {
            if (isMatchingAccount(transaction, accountNumber)) {
                accountTransactions.add(transaction);
            }
        }
        return accountTransactions;
    }

    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(allTransactions);
    }

    public int getTotalTransactions() {
        return allTransactions.size();
    }
    public void removeTransaction(String transactionId) {
        allTransactions.removeIf(transaction -> transaction.getTransactionId().equals(transactionId));
    }
    public Transaction getLastTransaction(String accountNumber) {
        if (allTransactions.isEmpty()) return null;
        for (int i = allTransactions.size() - 1; i >= 0; i--) {
            Transaction transaction = allTransactions.get(i);
            if (isMatchingAccount(transaction, accountNumber)) {
                return transaction;
            }
        }
        return null;
    }

    private double sumAmountsForTypes(String accountNumber, TransactionType... types) {
        if (allTransactions.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Transaction transaction : allTransactions) {
            if (!isMatchingAccount(transaction, accountNumber)) {
                continue;
            }
            if (matchesType(transaction, types)) {
                sum += transaction.getAmount();
            }
        }
        return sum;
    }

    private boolean isMatchingAccount(Transaction transaction, String accountNumber) {
        return transaction != null && accountNumber.equals(transaction.getAccountNumber());
    }

    private boolean matchesType(Transaction transaction, TransactionType... types) {
        if (types == null || types.length == 0) {
            return false;
        }
        String transactionType = transaction.getType();
        for (TransactionType type : types) {
            if (type.name().equalsIgnoreCase(transactionType)) {
                return true;
            }
        }
        return false;
    }

    private Transaction createTransaction(String accountNumber, TransactionType type, double amount, double balanceAfter) {
        return new Transaction(accountNumber, type.name(), amount, balanceAfter);
    }

}