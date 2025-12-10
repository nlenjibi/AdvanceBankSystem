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
    @DisplayName("Test Invalid Amount Exception Message")
    void testInvalidAmountExceptionMessage() {
        InvalidAmountException exception = new InvalidAmountException("Test error message");
        assertEquals("Test error message", exception.getMessage());
    }

    @Test
    @DisplayName("Test Insufficient Funds Exception Message")
    void testInsufficientFundsExceptionMessage() {
        InsufficientFundsException exception = new InsufficientFundsException("Test error message");
        assertEquals("Test error message", exception.getMessage());
    }

    @Test
    @DisplayName("Test Overdraft Exceeded Exception Message")
    void testOverdraftExceededExceptionMessage() {
        OverdraftExceededException exception = new OverdraftExceededException("Test error message");
        assertEquals("Test error message", exception.getMessage());
    }


    @Test
    @DisplayName("Test Savings Account Withdraw Below Minimum Throws Exception")
    void testSavingsAccountWithdrawBelowMinimumThrowsException() {
        SavingsAccount account = new SavingsAccount(customer, 150.0); // Just above minimum balance
        accountManager.addAccount(account);

        // Attempt to withdraw an amount that would bring the balance below the minimum
        RuntimeException runtimeEx = assertThrows(RuntimeException.class, () -> {
            transactionManager.withdraw("ACC001", 100.0); // This should fail because it would bring balance below minimum
        });

        Throwable cause = runtimeEx.getCause();
        assertTrue(cause instanceof InsufficientFundsException);

        // Verify the exception message contains relevant information
        String message = cause.getMessage();
        assertTrue(message.contains("Insufficient funds"));
        assertTrue(message.contains("$150.00")); // Current balance
        assertTrue(message.contains("Requested")); // Requested amount present
        assertTrue(message.contains("Min required")); // Minimum required present
    }

    @Test
    @DisplayName("Test Checking Account Overdraft Exceed Throws Exception")
    void testCheckingAccountOverdraftExceedThrowsException() {
        CheckingAccount account = new CheckingAccount(customer, 200.0);
        Transaction transaction = new Transaction(account.getAccountNumber(), TransactionType.WITHDRAWAL.name(), 200.0, account.getBalance());

        accountManager.addAccount(account);
        account.addTransaction(transaction);
        transactionManager.addTransaction(transaction);


// Attempt to withdraw an amount that exceeds the overdraft limit
        RuntimeException runtimeEx = assertThrows(RuntimeException.class, () -> {
            transactionManager.withdraw("ACC001", 800.0); // This exceeds the $500 overdraft limit
        });

        Throwable cause = runtimeEx.getCause();
        assertTrue(cause instanceof OverdraftExceededException);

// Verify the exception message contains relevant information
        String message = cause.getMessage();
        assertTrue(message.contains("Overdraft limit exceeded"));
        assertTrue(message.contains("$200.00")); // Current balance
        assertTrue(message.contains("$800.00")); // Requested amount
        assertTrue(message.contains("$500.00")); // Overdraft limit

    }

    @Test
    @DisplayName("Test Deposit Negative Amount Throws Invalid Amount Exception")
    void testDepositNegativeAmountThrowsInvalidAmountException() throws InvalidAmountException {
        SavingsAccount account = new SavingsAccount(customer, 1000.0);
        accountManager.addAccount(account);

        double initialBalance = account.getBalance();

        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () -> {
            transactionManager.deposit("ACC001", -100.0);
        });

        assertEquals("Deposit amount must be greater than 0", exception.getMessage());
        assertEquals(initialBalance, account.getBalance(), 0.01); // Balance should remain unchanged
    }

    @Test
    @DisplayName("Test Withdraw Negative Amount Throws Invalid Amount Exception")
    void testWithdrawNegativeAmountThrowsInvalidAmountException() throws InsufficientFundsException, OverdraftExceededException {
        SavingsAccount account = new SavingsAccount(customer,1000.0);
        accountManager.addAccount(account);

        double initialBalance = account.getBalance();

        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () -> {
            transactionManager.withdraw("ACC001", -50.0);
        });

        assertEquals("Withdrawal amount must be greater than 0", exception.getMessage());
        assertEquals(initialBalance, account.getBalance(), 0.01); // Balance should remain unchanged
    }

    @Test
    @DisplayName("Test Transfer Negative Amount Throws Invalid Amount Exception")
    void testTransferNegativeAmountThrowsInvalidAmountException() throws InsufficientFundsException, OverdraftExceededException {
        SavingsAccount fromAccount = new SavingsAccount(customer, 1000.0);
        CheckingAccount toAccount = new CheckingAccount(customer, 500.0);

        accountManager.addAccount(fromAccount);
        accountManager.addAccount(toAccount);

        double fromInitialBalance = fromAccount.getBalance();
        double toInitialBalance = toAccount.getBalance();

        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () -> {
            transactionManager.transfer("ACC001", "ACC002", -100.0);
        });

        assertEquals("Transfer amount must be greater than 0", exception.getMessage());
        assertEquals(fromInitialBalance, fromAccount.getBalance(), 0.01); // Balances should remain unchanged
        assertEquals(toInitialBalance, toAccount.getBalance(), 0.01);
    }

    @Test
    @DisplayName("Test Invalid Account Number Throws Exception")
    void testInvalidAccountNumberThrowsException() throws InvalidAmountException {
        // Try to deposit to a non-existent account
        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () -> {
            transactionManager.deposit("ACC999", 500.0);
        });

        assertEquals("Account not found: ACC999", exception.getMessage());
    }

    @Test
    @DisplayName("Test Invalid Source Account In Transfer Throws Exception")
    void testInvalidSourceAccountInTransferThrowsException() throws InvalidAmountException, InsufficientFundsException, OverdraftExceededException {
        CheckingAccount toAccount = new CheckingAccount(customer, 500.0);
        accountManager.addAccount(toAccount);

        // Try to transfer from a non-existent account
        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () -> {
            transactionManager.transfer("ACC999", "ACC002", 100.0);
        });

        assertEquals("Source account not found: ACC999", exception.getMessage());
    }

    @Test
    @DisplayName("Test Invalid Destination Account In Transfer Throws Exception")
    void testInvalidDestinationAccountInTransferThrowsException() throws InvalidAmountException, InsufficientFundsException, OverdraftExceededException {
        SavingsAccount fromAccount = new SavingsAccount(customer, 1000.0);
        accountManager.addAccount(fromAccount);

        // Try to transfer to a non-existent account
        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () -> {
            transactionManager.transfer("ACC001", "ACC999", 100.0);
        });

        assertEquals("Destination account not found: ACC999", exception.getMessage());
    }
}