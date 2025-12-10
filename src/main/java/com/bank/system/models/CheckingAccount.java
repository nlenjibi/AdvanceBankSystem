package com.bank.system.models;

import com.bank.system.enums.TransactionType;
import com.bank.system.exceptions.InvalidAmountException;
import com.bank.system.exceptions.OverdraftExceededException;


import static com.bank.system.utils.ConsoleUtil.printf;

public class CheckingAccount extends Account {
    private static final double OVERDRAFT_LIMIT = 500.0;
    private static final double MONTHLY_FEE = 10.0 ;


    public CheckingAccount(Customer customer, double initialBalance) {
        super(customer, initialBalance);

    }

    @Override
    public void displayAccountDetails() {
        printf("%-8s | %-15s | %-9s | $%,-9.2f | %-8s%n",
                getAccountNumber(),
                getCustomer().getName(),
                getAccountType(),
                getBalance(),
                getStatus());
        printf("%-8s | Overdraft Limit: $%.2f | Monthly Fee: $%,.2f%n",
                "",
                OVERDRAFT_LIMIT,
                MONTHLY_FEE);
    }


    @Override
    public String getAccountType() {
        return "Checking";
    }

    @Override
    public boolean withdraw(double amount) throws InvalidAmountException, OverdraftExceededException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than 0");
        }


        if (getBalance() + OVERDRAFT_LIMIT < amount) {
            throw new OverdraftExceededException(
                    String.format("Overdraft limit exceeded. Current balance: $%.2f, Requested: $%.2f, Overdraft limit: $%.2f",
                            getBalance(), amount, OVERDRAFT_LIMIT));
        }

        setBalance(getBalance() - amount);
        return true;
    }
    @Override
    public boolean deposit(double amount) throws InvalidAmountException {

        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than 0");
        }
        setBalance(getBalance() + amount);
        return true;
    }

    // Getters
    public double getOverdraftLimit() {
        return OVERDRAFT_LIMIT;
    }

    public double getMonthlyFee() {
        return MONTHLY_FEE;
    }
    public double getMaxWithdrawalAmount() {
        return getBalance() + OVERDRAFT_LIMIT;
    }


}