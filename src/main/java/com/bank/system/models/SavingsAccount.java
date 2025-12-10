package com.bank.system.models;

import com.bank.system.enums.TransactionType;
import com.bank.system.exceptions.InsufficientFundsException;
import com.bank.system.exceptions.InvalidAmountException;


import static com.bank.system.utils.ConsoleUtil.printf;
public class SavingsAccount extends Account  {
    private static final double INTEREST_RATE = 3.5;
    private static final double MINIMUM_BALANCE = 500.0;
    private static final double WITHDRAWAL_FEE = 2.0;

    public SavingsAccount(Customer customer, double initialBalance) {
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
        printf("%-8s | Interest Rate: %.1f%% | Min Balance: $%,.2f%n",
                "",
                INTEREST_RATE,
                MINIMUM_BALANCE);
    }

    @Override
    public String getAccountType() {
        return "Savings";
    }

    @Override
    public boolean withdraw(double amount) throws InvalidAmountException, InsufficientFundsException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than 0");
        }

        double withdrawalTotal = amount;
        if (getBalance() - withdrawalTotal < MINIMUM_BALANCE) {
            throw new InsufficientFundsException(
                    String.format(
                            "Insufficient funds. Current balance: $%.2f, Requested: $%.2f (incl. $%.2f fee), Min required: $%.2f",
                            getBalance(), withdrawalTotal, WITHDRAWAL_FEE, MINIMUM_BALANCE));
        }


        setBalance(getBalance() - withdrawalTotal);
        return true;
    }


    // Getters
    public double getInterestRate() {
        return INTEREST_RATE;
    }

    public double getMinimumBalance() {
        return MINIMUM_BALANCE;
    }
    @Override
    public boolean deposit(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than 0");
        }

        setBalance(getBalance() + amount);
        return true;
    }



}