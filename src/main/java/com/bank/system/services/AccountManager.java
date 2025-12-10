package com.bank.system.services;

import com.bank.system.models.Account;

import java.util.ArrayList;
import java.util.List;


import static com.bank.system.utils.ConsoleUtil.*;


public class AccountManager {
    private final List<Account> accounts;


    public AccountManager() {
        this.accounts = new ArrayList<>();
    }

    // Method to add an account
    public boolean addAccount(Account account) {
        if (account != null && account.getAccountNumber() != null ) {
            accounts.add(account);
            return true;
        }
        return false;
    }

    // Method to find an account by account number
    public Account findAccount(String accountNumber) {
        for (Account acct : accounts) {
            if (acct != null && acct.getAccountNumber().equals(accountNumber)) {
                return acct;
            }
        }
        return null; // Account not found
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
        for (Account acct : accounts) {
            if (acct != null) {
                acct.displayAccountDetails();
                printSeparator();
            }
        }

        printf("Total Accounts: %d%n", getTotalAccounts());
        printf("Total Bank Balance: $%,.2f%n", getTotalBalance());
        pressEnterToContinue(); // Wait for user to press Enter
    }

    // Method to get total balance of all accounts
    public double getTotalBalance() {
        return accounts.stream()
                .filter(acct -> acct != null)
                .mapToDouble(Account::getBalance)
                .sum();
    }

    public Account getAccount(String accountNumber) {
        return accounts.stream()
                .filter(acct -> acct.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElse(null);
    }

    public boolean accountExists(String accountNumber) {
        return accounts.stream()
                .anyMatch(acct -> acct.getAccountNumber().equals(accountNumber));
    }

   public List<Account> getAllAccounts() {
        return accounts;
    }

    public boolean removeAccount(String accountNumber) {
        return accounts.removeIf(acct -> acct.getAccountNumber().equals(accountNumber));
    }

    public  int getTotalAccounts() {
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
}

