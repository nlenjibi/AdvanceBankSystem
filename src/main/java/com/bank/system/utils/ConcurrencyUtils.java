package com.bank.system.utils;

import com.bank.system.models.Account;
import com.bank.system.services.AccountManager;
import com.bank.system.services.TransactionManager;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static com.bank.system.utils.ConsoleUtil.*;
import static com.bank.system.utils.ValidationUtils.*;

public class ConcurrencyUtils {
    private static final Random random = new Random();

    /**
     * Runs a concurrent transaction simulation with multiple threads performing
     * deposits and withdrawals on accounts
     */
    public static void runConcurrentSimulation(AccountManager accountManager, TransactionManager transactionManager, int numThreads) {
        print("Running concurrent transaction simulation...");
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        // Get a sample account for the simulation
        Account sampleAccount = accountManager.getAllAccounts().stream().findFirst().orElse(null);
        
        if (sampleAccount == null) {
            System.out.println("No accounts available for simulation.");
            executor.shutdown();
            return;
        }
        
        String accountNumber = sampleAccount.getAccountNumber();
        print("Using account: " + accountNumber + " for simulation");
        
        // Submit multiple tasks to perform transactions concurrently
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i + 1;
            executor.submit(() -> {
                String threadName = "Thread-" + threadId;
                
                // Perform random transactions
                for (int j = 0; j < 3; j++) { // Each thread performs 3 transactions
                    double amount = 100 + random.nextDouble() * 400; // Random amount between 100-500
                    
                    // Randomly decide whether to deposit or withdraw
                    if (random.nextBoolean()) {
                        print(threadName + ": Depositing $" + String.format("%.2f", amount) + " to " + accountNumber);
                        transactionManager.deposit(accountNumber, amount);
                    } else {
                        System.out.println(threadName + ": Withdrawing $" + String.format("%.2f", amount) + " from " + accountNumber);
                        transactionManager.withdraw(accountNumber, amount);
                    }
                    
                    // Small delay to simulate real-world timing
                    try {
                        Thread.sleep(random.nextInt(100) + 50); // Random delay 50-150ms
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }
        
        // Shutdown the executor and wait for all tasks to complete
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // Print final balance
        Account finalAccount = accountManager.findAccount(accountNumber);
        if (finalAccount != null) {
            print("\nThread-safe operations completed successfully.");
            print("Final Balance for " + accountNumber + ": $" + String.format("%.2f", finalAccount.getBalance()));
        }
    }
    
    
     //Simulates concurrent access to multiple accounts

    public static void runMultiAccountConcurrentSimulation(AccountManager accountManager, TransactionManager transactionManager, int numThreads) {
        print("Running multi-account concurrent transaction simulation...");
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        var accounts = accountManager.getAllAccounts();
        if (accounts.isEmpty()) {
           print ("No accounts available for simulation.");
            executor.shutdown();
            return;
        }
        
        // Convert to array for easier indexing
        var accountArray = accounts.toArray(new Account[0]);
        
        // Submit multiple tasks to perform transactions on different accounts
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i + 1;
            final int index = i; // Make index effectively final for lambda
            executor.submit(() -> {
                String threadName = "Thread-" + threadId;
                
                // Each thread works on a different account (or cycles through accounts)
                Account targetAccount = accountArray[index % accountArray.length];
                String accountNumber = targetAccount.getAccountNumber();
                
                // Perform random transactions
                for (int j = 0; j < 2; j++) { // Each thread performs 2 transactions
                    double amount = 50 + random.nextDouble() * 200; // Random amount between 50-250
                    
                    // Randomly decide whether to deposit or withdraw
                    if (random.nextBoolean()) {
                        System.out.println(threadName + ": Depositing $" + String.format("%.2f", amount) + " to " + accountNumber);
                        transactionManager.deposit(accountNumber, amount);
                    } else {
                        System.out.println(threadName + ": Withdrawing $" + String.format("%.2f", amount) + " from " + accountNumber);
                        transactionManager.withdraw(accountNumber, amount);
                    }
                    
                    // Small delay to simulate real-world timing
                    try {
                        Thread.sleep(random.nextInt(100) + 25); // Random delay 25-125ms
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }
        
        // Shutdown the executor and wait for all tasks to complete
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("\nMulti-account thread-safe operations completed successfully.");
        
        // Print final balances for all accounts
        for (Account account : accountManager.getAllAccounts()) {
            System.out.println("Final Balance for " + account.getAccountNumber() + ": $" + String.format("%.2f", account.getBalance()));
        }
    }
}