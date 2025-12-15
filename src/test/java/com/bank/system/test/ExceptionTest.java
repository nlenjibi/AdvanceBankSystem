package com.bank.system.test;

import com.bank.system.enums.TransactionType;
import com.bank.system.exceptions.*;
import com.bank.system.models.*;
import com.bank.system.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExceptionTest {
    private AccountManager accountManager;
    private TransactionManager transactionManager;
    private static RegularCustomer customer;

    @BeforeEach
    void setUp() {
        accountManager = new AccountManager();
        transactionManager = new TransactionManager(accountManager);
        customer = new RegularCustomer("John Smith", 34,"1234567890", "box 3");
    }


    @Test
    @DisplayName("Test Deposit Negative Amount Throws Invalid Amount Exception")
    void testDepositNegativeAmountThrowsInvalidAmountException() {
        SavingsAccount account = createSavingsAccount(1000.0);
        double initialBalance = account.getBalance();

        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () -> {
            transactionManager.deposit(account.getAccountNumber(), -100.0);
        });

        assertEquals("Deposit amount must be greater than 0", exception.getMessage());
        assertEquals(initialBalance, account.getBalance(), 0.01); // Balance should remain unchanged
    }

    @Test
    @DisplayName("Test Withdraw Negative Amount Throws Invalid Amount Exception")
    void testWithdrawNegativeAmountThrowsInvalidAmountException()  {
        SavingsAccount account = createSavingsAccount(1000.0);
        double initialBalance = account.getBalance();

        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () -> {
            transactionManager.withdraw(account.getAccountNumber(), -50.0);
        });

        assertEquals("Withdrawal amount must be greater than 0", exception.getMessage());
        assertEquals(initialBalance, account.getBalance(), 0.01); // Balance should remain unchanged
    }

    @Test
    @DisplayName("Test Transfer Negative Amount Throws Invalid Amount Exception")
    void testTransferNegativeAmountThrowsInvalidAmountException()  {
        SavingsAccount fromAccount = createSavingsAccount(1000.0);
        CheckingAccount toAccount = createCheckingAccount(500.0);

        double fromInitialBalance = fromAccount.getBalance();
        double toInitialBalance = toAccount.getBalance();

        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () -> {
            transactionManager.transfer(fromAccount.getAccountNumber(), toAccount.getAccountNumber(), -100.0);
        });

        assertEquals("Transfer amount must be greater than 0", exception.getMessage());
        assertEquals(fromInitialBalance, fromAccount.getBalance(), 0.01); // Balances should remain unchanged
        assertEquals(toInitialBalance, toAccount.getBalance(), 0.01);
    }




    private SavingsAccount createSavingsAccount(double initialBalance) {
        SavingsAccount account = new SavingsAccount(customer, initialBalance);
        accountManager.addAccount(account);
        return account;
    }

    private CheckingAccount createCheckingAccount(double initialBalance) {
        CheckingAccount account = new CheckingAccount(customer, initialBalance);
        accountManager.addAccount(account);
        return account;
    }

}