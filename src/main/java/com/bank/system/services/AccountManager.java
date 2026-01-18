package com.bank.system.services;

import com.bank.system.models.Account;
import com.bank.system.models.CheckingAccount;
import com.bank.system.models.Customer;
import com.bank.system.models.SavingsAccount;

import java.util.Collection;
import java.util.HashMap;
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
        this.accounts = new HashMap<>();
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



    /**
     * Gets the total balance across all accounts
     */


    public Account getAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public boolean accountExists(String accountNumber) {
        return accounts.containsKey(accountNumber);
    }


    public synchronized boolean removeAccount(String accountNumber) {
        return accounts.remove(accountNumber) != null;
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
                .toList();
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
