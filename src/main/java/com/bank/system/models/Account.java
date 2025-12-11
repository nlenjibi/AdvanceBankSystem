package com.bank.system.models;
import com.bank.system.enums.TransactionType;
import com.bank.system.exceptions.InsufficientFundsException;
import com.bank.system.exceptions.InvalidAmountException;
import com.bank.system.exceptions.OverdraftExceededException;
import com.bank.system.interfaces.Transactable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public abstract class Account implements Transactable {
    private final String accountNumber;
    private final Customer customer;
    private double balance;
    private final String status;
    protected final List<Transaction> transactions;
    private static final AtomicInteger ACCOUNT_COUNTER = new AtomicInteger(0);

    protected Account(Customer customer, double initialDeposit) {
        this.customer = customer;
        this.balance = initialDeposit;
        this.status = "Active";
        this.accountNumber = generateAccountNumber();
        this.transactions = new ArrayList<>();
    }

    private static String generateAccountNumber() {
        return String.format("ACC%03d", ACCOUNT_COUNTER.incrementAndGet());
    }

    // Abstract methods to be implemented by subclasses
    public abstract void displayAccountDetails();

    public abstract String getAccountType();



    // Abstract methods to be implemented by subclasses
    public abstract boolean withdraw(double amount) throws InsufficientFundsException, InvalidAmountException, OverdraftExceededException;

    public boolean deposit(double amount) throws InvalidAmountException {

        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than 0");
        }
        setBalance(getBalance() + amount);
        return true;
    }
    // Withdraw method - to be overridden by subclasses

    // Getters and setters
    public String getAccountNumber() {
        return accountNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public void addTransaction(Transaction transaction) {
        if (transaction != null) {
            transactions.add(transaction);
        }
    }
    public boolean removeTransaction(Transaction transaction) {
        return transaction != null && transactions.remove(transaction);
    }

    public boolean removeTransactionById(String transactionId) {
        if (transactionId == null) {
            return false;
        }
        return transactions.removeIf(t -> transactionId.equals(t.getTransactionId()));
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }


    @Override
    public boolean processTransaction(double amount, TransactionType type) {
        if (type == null) {
            return false;
        }
        return switch (type) {
            case DEPOSIT -> executeTransaction(() -> deposit(amount));
            case WITHDRAWAL -> executeTransaction(() -> withdraw(amount));
            default -> false;
        };
    }

    private boolean executeTransaction(TransactionCommand command) {
        try {
            return command.run();
        } catch (InvalidAmountException | InsufficientFundsException | OverdraftExceededException e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    private interface TransactionCommand {
        boolean run() throws InvalidAmountException, InsufficientFundsException, OverdraftExceededException;
    }


}