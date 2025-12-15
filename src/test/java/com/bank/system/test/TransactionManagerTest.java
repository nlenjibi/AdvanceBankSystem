package com.bank.system.test;

import com.bank.system.exceptions.*;
import com.bank.system.models.*;
import com.bank.system.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TransactionManagerTest {
    private AccountManager accountManager;
    private TransactionManager transactionManager;
    private RegularCustomer customer;
    private PremiumCustomer premiumCustomer;

    @BeforeEach
    void setUp() {
        accountManager = new AccountManager();
        transactionManager = new TransactionManager(accountManager);
        customer = new RegularCustomer("John Smith", 43, "1234567890", "box 3");
        premiumCustomer = new PremiumCustomer("Jane Doe", 34, "1234567890", "box 3s");
    }

    @Test
    @DisplayName("Test Deposit Updates Balance")
    void testDepositUpdatesBalance() throws InvalidAmountException {
        SavingsAccount account = createSavingsAccount(1000.0);

        double initialBalance = account.getBalance();
        transactionManager.deposit(account.getAccountNumber(), 500.0);

        assertEquals(initialBalance + 500.0, account.getBalance(), 0.01);
    }

    @Test
    @DisplayName("Test Deposit Invalid Amount")
    void testDepositInvalidAmount() {
        SavingsAccount account = createSavingsAccount(1000.0);
        double initialBalance = account.getBalance();

        assertThrows(InvalidAmountException.class, () -> transactionManager.deposit(account.getAccountNumber(), -100.0));
        assertThrows(InvalidAmountException.class, () -> transactionManager.deposit(account.getAccountNumber(), 0.0));

        assertBalanceUnchanged(account, initialBalance);
    }

    @Test
    @DisplayName("Test Withdraw Invalid Amount")
    void testWithdrawInvalidAmount() {
        SavingsAccount account = createSavingsAccount(1000.0);
        double initialBalance = account.getBalance();

        assertThrows(InvalidAmountException.class, () -> transactionManager.withdraw(account.getAccountNumber(), -50.0));
        assertThrows(InvalidAmountException.class, () -> transactionManager.withdraw(account.getAccountNumber(), 0.0));

        assertBalanceUnchanged(account, initialBalance);
    }

    @Test
    @DisplayName("Test Withdraw Insufficient Funds")
    void testWithdrawInsufficientFunds() {
        SavingsAccount account = createSavingsAccount(500.0); // Just above minimum balance

        RuntimeException ex = assertThrows(RuntimeException.class, () -> transactionManager.withdraw(account.getAccountNumber(), 100.0));
        assertEquals(InsufficientFundsException.class, ex.getCause().getClass());

        assertBalanceUnchanged(account, 500.0);
    }

    @Test
    @DisplayName("Test Transfer Between Accounts")
    void testTransferBetweenAccounts() throws  InvalidAmountException{
        SavingsAccount fromAccount = createSavingsAccount(1000.0);
        CheckingAccount toAccount = createCheckingAccount(500.0);

        double fromInitialBalance = fromAccount.getBalance();
        double toInitialBalance = toAccount.getBalance();

        transactionManager.transfer(fromAccount.getAccountNumber(), toAccount.getAccountNumber(), 300.0);

        assertEquals(fromInitialBalance - 300.0, fromAccount.getBalance(), 0.01);
        assertEquals(toInitialBalance + 300.0, toAccount.getBalance(), 0.01);
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

    private void assertBalanceUnchanged(Account account, double referenceBalance) {
        assertEquals(referenceBalance, account.getBalance(), 0.01);
    }
}