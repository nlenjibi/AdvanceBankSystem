package com.bank.system.test;

import com.bank.system.models.*;
import com.bank.system.services.FilePersistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilePersistenceTest {
    private static final Path ACCOUNTS_PATH = Paths.get("data/accounts.txt");
    private static final Path TRANSACTIONS_PATH = Paths.get("data/transactions.txt");

    private FilePersistence persistence;

    @BeforeEach
    void setUp() throws IOException {
        persistence = new FilePersistence();
        deleteIfExists(ACCOUNTS_PATH);
        deleteIfExists(TRANSACTIONS_PATH);
    }

    @AfterEach
    void tearDown() throws IOException {
        deleteIfExists(ACCOUNTS_PATH);
        deleteIfExists(TRANSACTIONS_PATH);
    }

    @Test
    @DisplayName("saveAccounts writes data that loadAccounts can read")
    void saveAccountsAndLoadAccounts() {
        RegularCustomer customer = new RegularCustomer("John Smith", 35, "1234567890", "123 Main St");
        SavingsAccount account = new SavingsAccount(customer, 1500.0);
        Map<String, Account> accounts = Map.of(account.getAccountNumber(), account);

        persistence.saveAccounts(accounts);

        Map<String, Account> loaded = persistence.loadAccounts();

        assertEquals(1, loaded.size());
        Account loadedAccount = loaded.get(account.getAccountNumber());
        assertNotNull(loadedAccount);
        assertEquals(account.getAccountNumber(), loadedAccount.getAccountNumber());
        assertEquals(account.getBalance(), loadedAccount.getBalance(), 0.01);
        assertEquals(account.getCustomer().getName(), loadedAccount.getCustomer().getName());
        assertEquals(account.getCustomer().getAddress(), loadedAccount.getCustomer().getAddress());
    }

    @Test
    @DisplayName("loadAccounts returns empty when no file exists")
    void loadAccountsReturnsEmptyWhenMissing() {
        Map<String, Account> loaded = persistence.loadAccounts();

        assertTrue(loaded.isEmpty());
    }

    @Test
    @DisplayName("saveTransactions writes data that loadTransactions can read")
    void saveTransactionsAndLoadTransactions() {
        Transaction transaction = new Transaction("TXN999", "ACC123", "DEPOSIT", 200.0, 1200.0, "15-12-2025 10:00 AM");
        List<Transaction> transactions = List.of(transaction);

        persistence.saveTransactions(transactions);

        List<Transaction> loaded = persistence.loadTransactions();

        assertEquals(1, loaded.size());
        Transaction loadedTransaction = loaded.get(0);
        assertEquals(transaction.getTransactionId(), loadedTransaction.getTransactionId());
        assertEquals(transaction.getAccountNumber(), loadedTransaction.getAccountNumber());
        assertEquals(transaction.getType(), loadedTransaction.getType());
        assertEquals(transaction.getAmount(), loadedTransaction.getAmount(), 0.01);
        assertEquals(transaction.getBalanceAfter(), loadedTransaction.getBalanceAfter(), 0.01);
        assertEquals(transaction.getTimestamp(), loadedTransaction.getTimestamp());
    }

    @Test
    @DisplayName("loadTransactions returns empty when no file exists")
    void loadTransactionsReturnsEmptyWhenMissing() {
        List<Transaction> loaded = persistence.loadTransactions();

        assertTrue(loaded.isEmpty());
    }

    private void deleteIfExists(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }
}