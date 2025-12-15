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
        SavingsAccount account = createSavingsAccount(1000.0);
        double initialBalance = account.getBalance();

        account.deposit(500.0);

        assertEquals(initialBalance + 500.0, account.getBalance(), 0.01);
    }

    @Test
    @DisplayName("Test Savings Account Deposit Invalid Amount")
    void testSavingsAccountDepositInvalidAmount() {
        SavingsAccount account = createSavingsAccount(1000.0);
        double initialBalance = account.getBalance();

        assertThrows(InvalidAmountException.class, () -> account.deposit(-100.0));
        assertThrows(InvalidAmountException.class, () -> account.deposit(0.0));

        assertBalanceUnchanged(account, initialBalance);
    }

    @Test
    @DisplayName("Test Savings Account Withdraw")
    void testSavingsAccountWithdraw() throws InsufficientFundsException, InvalidAmountException {
        SavingsAccount account = createSavingsAccount(1000.0);
        double initialBalance = account.getBalance();

        boolean success = account.withdraw(200.0);

        assertTrue(success);
        assertEquals(initialBalance - 200.0, account.getBalance(), 0.01);
    }

    @Test
    @DisplayName("Test Savings Account Withdraw Invalid Amount")
    void testSavingsAccountWithdrawInvalidAmount() {
        SavingsAccount account = createSavingsAccount(1000.0);
        double initialBalance = account.getBalance();

        assertThrows(InvalidAmountException.class, () -> account.withdraw(-50.0));
        assertThrows(InvalidAmountException.class, () -> account.withdraw(0.0));

        assertBalanceUnchanged(account, initialBalance);
    }

    @Test
    @DisplayName("Test Savings Account Withdraw Insufficient Funds")
    void testSavingsAccountWithdrawInsufficientFunds() {
        SavingsAccount account = createSavingsAccount(600.0);

        assertThrows(InsufficientFundsException.class, () -> account.withdraw(150.0));
    }

    @Test
    @DisplayName("Test Checking Account Deposit")
    void testCheckingAccountDeposit() throws InvalidAmountException {
        CheckingAccount account = createCheckingAccount(1000.0);
        double initialBalance = account.getBalance();

        account.deposit(300.0);

        assertEquals(initialBalance + 300.0, account.getBalance(), 0.01);
    }

    @Test
    @DisplayName("Test Checking Account Deposit Invalid Amount")
    void testCheckingAccountDepositInvalidAmount() {
        CheckingAccount account = createCheckingAccount(1000.0);
        double initialBalance = account.getBalance();

        assertThrows(InvalidAmountException.class, () -> account.deposit(-200.0));
        assertThrows(InvalidAmountException.class, () -> account.deposit(0.0));

        assertBalanceUnchanged(account, initialBalance);
    }

    @Test
    @DisplayName("Test Checking Account Withdraw Within Overdraft")
    void testCheckingAccountWithdrawWithinOverdraft() throws InvalidAmountException, OverdraftExceededException {
        CheckingAccount account = createCheckingAccount(200.0);
        double initialBalance = account.getBalance();

        boolean success = account.withdraw(600.0);

        assertTrue(success);
        assertEquals(initialBalance - 600.0, account.getBalance(), 0.01);
    }

    private SavingsAccount createSavingsAccount(double balance) {
        return new SavingsAccount(customer, balance);
    }

    private CheckingAccount createCheckingAccount(double balance) {
        return new CheckingAccount(premiumCustomer, balance);
    }

    private void assertBalanceUnchanged(Account account, double expectedBalance) {
        assertEquals(expectedBalance, account.getBalance(), 0.01);
    }
}