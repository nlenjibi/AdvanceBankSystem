package com.bank.system.services;

import com.bank.system.models.Account;
import com.bank.system.models.CheckingAccount;
import com.bank.system.models.Customer;
import com.bank.system.models.SavingsAccount;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.bank.system.utils.ConsoleUtil.*;


public class AccountManager {
    private final Map<String, Account> accounts;
    private int nextAccountId = 1;

    public AccountManager() {
        this.accounts = new ConcurrentHashMap<>();
    }

    // Method to add an account
    public boolean addAccount(Account account) {
        if (account == null || account.getAccountNumber() == null) {
            return false;
        }
        accounts.put(account.getAccountNumber(), account);
        return true;
    }

    // Method to find an account by account number
    public Account findAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public Collection<Account> getAllAccounts() {
        return accounts.values();
    }


    // Method to view all accounts
    public void viewAllAccounts() {
        if (getTotalAccounts() == 0) {
            print("No accounts found.");
            pressEnterToContinue();
            return;
        }
        print(" ");
        printHeader("ACCOUNT LISTING");
        printSeparator();
        printf("%-8s | %-15s | %-9s | %-10s | %-8s%n",
                "ACC NO", "CUSTOMER NAME", "TYPE", "BALANCE", "STATUS");
        printSeparator();
        for (Account acct : accounts.values()) {
            acct.displayAccountDetails();
            printSeparator();
        }

        printf("Total Accounts: %d%n", getTotalAccounts());
        printf("Total Bank Balance: $%,.2f%n", getTotalBalance());
        pressEnterToContinue(); // Wait for user to press Enter
    }

    /**
     * Gets the total balance across all accounts
     */
    public double getTotalBalance() {
        return accounts.values().stream()
                .mapToDouble(Account::getBalance)
                .sum();
    }

    public Account getAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public boolean accountExists(String accountNumber) {
        return accounts.containsKey(accountNumber);
    }


    public synchronized boolean removeAccount(String accountNumber) {
        return accounts.remove(accountNumber) != null;
    }

    public List<Account> searchAccounts(Predicate<Account> condition) {
        return accounts.values().stream()
                .filter(condition)
                .collect(Collectors.toList());
    }


    public int getTotalAccounts() {
        return accounts.size();
    }


    public void displayAccountDetails(Account account) {
        if (account == null) {
            print("Account not found!");
            pressEnterToContinue();
            return;
        }

        print(" ");
        print("Account Details:");
        print("Customer: " + account.getCustomer().getName());
        print("Account Type: " + account.getAccountType());
        printf("Current Balance: $%,.2f%n", account.getBalance());
    }

    /**
     * Gets the average account balance
     */
    public double getAverageBalance() {
        if (accounts.isEmpty()) {
            return 0.0;
        }
        return accounts.values().stream()
                .mapToDouble(Account::getBalance)
                .average()
                .orElse(0.0);
    }

    /**
     * Gets accounts sorted by balance (descending)
     */
    public List<Account> getAccountsSortedByBalance() {
        return accounts.values().stream()
                .sorted((a, b) -> Double.compare(b.getBalance(), a.getBalance()))
                .collect(Collectors.toList());
    }

    /**
     * Gets accounts by account type
     */
    public List<Account> getAccountsByType(String accountType) {
        return accounts.values().stream()
                .filter(account -> {
                    if ("savings".equalsIgnoreCase(accountType)) {
                        return account instanceof SavingsAccount;
                    } else if ("checking".equalsIgnoreCase(accountType)) {
                        return account instanceof CheckingAccount;
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    /**
     * Updates customer information for an account
     */
    public synchronized boolean updateCustomerInfo(String accountNumber, String name, int age, String contact, String address) {
        Account account = accounts.get(accountNumber);
        if (account != null) {
            Customer customer = account.getCustomer();
            customer.setName(name);
            customer.setContact(contact);
            customer.setAddress(address);
            customer.setAge(age);
            return true;
        }
        return false;
    }

    public Map<String, Account> getAccountsMap() {
        return accounts;
    }


}
