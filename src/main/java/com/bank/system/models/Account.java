package com.bank.system.models;
import com.bank.system.enums.TransactionType;
import com.bank.system.exceptions.InsufficientFundsException;
import com.bank.system.exceptions.InvalidAmountException;
import com.bank.system.exceptions.OverdraftExceededException;
import com.bank.system.interfaces.Transactable;


import java.util.concurrent.atomic.AtomicInteger;


public abstract class Account implements Transactable {
    private String accountNumber;
    private final Customer customer;
    private double balance;
    private final String status;

    private static final AtomicInteger ACCOUNT_COUNTER = new AtomicInteger(0);


    protected Account(String accountNumber, Customer customer, double initialDeposit) {
        this.customer = customer;
        this.balance = initialDeposit;
        this.status = "Active";
        this.accountNumber = accountNumber;
        syncAccountCounter(accountNumber);

    }
    private static void syncAccountCounter(String accountNumber) {
        if (accountNumber != null && accountNumber.startsWith("ACC")) {
            try {
                int value = Integer.parseInt(accountNumber.substring(3));
                ACCOUNT_COUNTER.updateAndGet(current -> Math.max(current, value));
            } catch (NumberFormatException ignored) {
            }
        }
    }
    protected static String generateAccountNumber() {
        return String.format("ACC%03d", ACCOUNT_COUNTER.incrementAndGet());
    }

    // Abstract methods to be implemented by subclasses
    public abstract void displayAccountDetails();

    public abstract String getAccountType();



    // Abstract methods to be implemented by subclasses
    public  abstract  boolean withdraw(double amount) throws InsufficientFundsException, InvalidAmountException, OverdraftExceededException;

    public synchronized boolean deposit(double amount) throws InvalidAmountException {
        ensurePositiveAmount(amount, "Deposit");
        setBalance(getBalance() + amount);
        return true;
    }

    protected final void ensurePositiveAmount(double amount, String context) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException(context + " amount must be greater than 0");
        }
    }
    // Withdraw method - to be overridden by subclasses

    // Getters and setters
    public String getAccountNumber() {
        return accountNumber;
    }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;

    }

    public Customer getCustomer() {
        return customer;
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

    public boolean isActive() {
        return status.equals("Active");
    }

    @FunctionalInterface
    private interface TransactionCommand {
        boolean run() throws InvalidAmountException, InsufficientFundsException, OverdraftExceededException;
    }


}