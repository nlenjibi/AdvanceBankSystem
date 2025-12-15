package com.bank.system.test;

import com.bank.system.exceptions.*;
import com.bank.system.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {
    private RegularCustomer customer;
    private PremiumCustomer premiumCustomer;

    @BeforeEach
    void setUp() {
        customer = new RegularCustomer("John Smith", 35, "1234567890", "123 Main St");
        premiumCustomer = new PremiumCustomer("Jane Doe", 40, "0987654321", "456 Oak Ave");
    }

    @Test
    @DisplayName("Test Savings Account Deposit")
    void testSavingsAccountDeposit() throws InvalidAmountException {
        SavingsAccount account = new SavingsAccount("ACC001", customer, 1000.0);
        double initialBalance = account.getBalance();

        account.deposit(500.0);

        assertEquals(initialBalance + 500.0, account.getBalance(), 0.01);
    }

    @Test
    @DisplayName("Test Savings Account Deposit Invalid Amount")
    void testSavingsAccountDepositInvalidAmount() {
        SavingsAccount account = new SavingsAccount("ACC001",customer, 1000.0);
        double initialBalance = account.getBalance();

        assertThrows(InvalidAmountException.class, () -> account.deposit(-100.0));
        assertThrows(InvalidAmountException.class, () -> account.deposit(0.0));
        assertEquals(initialBalance, account.getBalance(), 0.01); // Balance should remain unchanged
    }

    @Test
    @DisplayName("Test Savings Account Withdraw")
    void testSavingsAccountWithdraw() throws InsufficientFundsException, InvalidAmountException {
        SavingsAccount account = new SavingsAccount("ACC001", customer, 1000.0);
        double initialBalance = account.getBalance();

        boolean success = account.withdraw(200.0);

        assertTrue(success);
        assertEquals(initialBalance - 200.0 - 2.0, account.getBalance(), 0.01); // 2.0 is withdrawal fee
    }

    @Test
    @DisplayName("Test Savings Account Withdraw Invalid Amount")
    void testSavingsAccountWithdrawInvalidAmount() {
        SavingsAccount account = new SavingsAccount("ACC001",customer, 1000.0);
        double initialBalance = account.getBalance();

        assertThrows(InvalidAmountException.class, () -> account.withdraw(-50.0));
        assertThrows(InvalidAmountException.class, () -> account.withdraw(0.0));
        assertEquals(initialBalance, account.getBalance(), 0.01); // Balance should remain unchanged
    }

    @Test
    @DisplayName("Test Savings Account Withdraw Insufficient Funds")
    void testSavingsAccountWithdrawInsufficientFunds() {
        SavingsAccount account = new SavingsAccount("ACC001", customer, 600.0);
        assertThrows(InsufficientFundsException.class, () -> account.withdraw(150.0));
    }

    @Test
    @DisplayName("Test Checking Account Deposit")
    void testCheckingAccountDeposit() throws InvalidAmountException {
        CheckingAccount account = new CheckingAccount("ACC001", premiumCustomer, 1000.0);
        double initialBalance = account.getBalance();

        account.deposit(300.0);

        assertEquals(initialBalance + 300.0, account.getBalance(), 0.01);
    }

    @Test
    @DisplayName("Test Checking Account Deposit Invalid Amount")
    void testCheckingAccountDepositInvalidAmount() {
        CheckingAccount account = new CheckingAccount("ACC001", premiumCustomer, 1000.0);
        double initialBalance = account.getBalance();

        assertThrows(InvalidAmountException.class, () -> account.deposit(-200.0));
        assertThrows(InvalidAmountException.class, () -> account.deposit(0.0));
        assertEquals(initialBalance, account.getBalance(), 0.01); // Balance should remain unchanged
    }

    @Test
    @DisplayName("Test Checking Account Withdraw Within Overdraft")
    void testCheckingAccountWithdrawWithinOverdraft() throws InvalidAmountException, OverdraftExceededException {
        CheckingAccount account = new CheckingAccount("ACC001", premiumCustomer, 200.0);
        double initialBalance = account.getBalance();

        boolean success = account.withdraw(600.0); // Within $500 overdraft limit

        assertTrue(success);
        assertEquals(initialBalance - 600.0, account.getBalance(), 0.01);
    }

    @Test
    @DisplayName("Test Checking Account Withdraw Exceeds Overdraft")
    void testCheckingAccountWithdrawExceedsOverdraft() {
        CheckingAccount account = new CheckingAccount("ACC001",premiumCustomer, 200.0);
        assertThrows(OverdraftExceededException.class, () -> account.withdraw(800.0));
    }

    @Test
    @DisplayName("Test Savings Account Minimum Balance")
    void testSavingsAccountMinimumBalance() {
        SavingsAccount account = new SavingsAccount("ACC001",customer, 1000.0);
        assertEquals(500.0, account.getMinimumBalance());
    }

    @Test
    @DisplayName("Test Checking Account Overdraft Limit")
    void testCheckingAccountOverdraftLimit() {
        CheckingAccount account = new CheckingAccount("ACC001", premiumCustomer, 1000.0);
        assertEquals(500.0, account.getOverdraftLimit());
    }

    @Test
    @DisplayName("Test Customer Interest Rates")
    void testCustomerInterestRates() {
        assertEquals(0.035, customer.getInterestRate());
        assertEquals(0.035, premiumCustomer.getInterestRate());
    }
}