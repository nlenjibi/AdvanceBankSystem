package com.bank.system.processes;


import com.bank.system.enums.TransactionType;
import com.bank.system.models.Account;
import com.bank.system.models.CheckingAccount;
import com.bank.system.models.Customer;
import com.bank.system.models.PremiumCustomer;
import com.bank.system.models.RegularCustomer;
import com.bank.system.models.SavingsAccount;
import com.bank.system.models.Transaction;
import com.bank.system.services.AccountManager;
import com.bank.system.services.TransactionManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.bank.system.utils.ConsoleUtil.*;
import static com.bank.system.utils.ConsoleUtil.printSeparator;
import static com.bank.system.utils.ValidationUtils.isValidAddress;
import static com.bank.system.utils.ValidationUtils.isValidAccountNumber;
import static com.bank.system.utils.ValidationUtils.isValidName;
import static com.bank.system.utils.ValidationUtils.isValidPhone;


public class AccountProcessHandler {
    private static final double REGULAR_MIN_DEPOSIT = 500;
    private static final double PREMIUM_MIN_DEPOSIT = 10000;
    private final TransactionManager transactionManager;
    private final AccountManager accountManager;

    public AccountProcessHandler(AccountManager accountManager, TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.accountManager = accountManager;
    }
    private record CustomerData(String name, int age, String contact, String address) {}
    private record AccountCreation(Account account, double initialDeposit) {}

    public void createAccount() {
        print(" ");
        print("ACCOUNT CREATION");
        print(" ");

        Customer customer = createCustomerFromData(readCustomerDetails());
        AccountCreation creation = selectAccountType(customer);

        if (persistNewAccount(creation)) {
            displayAccountCreatedInfo(creation.account(), customer);
        } else {
            print("Failed to create account. Maximum account limit reached.");
        }
        print(" ");
        pressEnterToContinue();
    }
    private CustomerData readCustomerDetails() {
        String name = readString("Enter Customer Name: ",
                isValidName,
                "Error: Invalid name format. Name should contain only letters and spaces.");

        int age = getValidIntInput("Enter customer age: ", 1, 150);

        String contact = readString("Enter customer Phone Number: ",
                isValidPhone,
                "Error: Invalid phone format. Please enter a valid phone number.");

        String address = readString("Enter customer address: ",
                isValidAddress,
                "Error: Invalid address format. Address should contain only letters, numbers, and spaces.");

        return new CustomerData(name, age, contact, address);
    }

    private void displayAccountCreatedInfo(Account account, Customer customer) {
        print(" ");
        print("✓ Account created successfully!");
        print("Account Number: " + account.getAccountNumber());
        print("Customer: " + customer.getName() + " (" + customer.getCustomerType() + ")");
        print("Account Type: " + account.getAccountType());
        printf("Initial Balance: $%.2f%n", account.getBalance());

        if (account instanceof SavingsAccount savings) {
            printf("Interest Rate: %.1f%%%n", savings.getInterestRate());
            printf("Minimum Balance: $%,.2f%n", savings.getMinimumBalance());
        } else if (account instanceof CheckingAccount checking) {
            printf("Overdraft Limit: $%,.2f%n", checking.getOverdraftLimit());
            if (customer instanceof PremiumCustomer) {
                print("Monthly Fee: Waived (Premium Customer)");
            } else {
                printf("Monthly Fee: $%,.2f%n", checking.getMonthlyFee());
            }
        }
        print("Status: " + account.getStatus());
    }
    private AccountCreation selectAccountType(Customer customer) {
        print(" ");
        print("Account type:");

        double savingsMin = minimumSavingsDeposit(customer);
        print("1. Savings Account (Interest: 3.5% Min Balance: $" + String.format("%,.0f", savingsMin) + ")");
        print("2. Checking Account (Overdraft: $1,000, Monthly Fee: $10)");
        int accountType = getValidIntInput("Select type (1-2): ", 1, 2);
        double initialDeposit = getValidDoubleInput(
                "Enter initial deposit amount: $",
                v -> v >= savingsMin,
                "Value must be greater than or equal to $" + String.format("%,.0f", savingsMin) + "."
        );

        Account account = (accountType == 2)
                ? new CheckingAccount(customer, initialDeposit)
                : new SavingsAccount(customer, initialDeposit);

        return new AccountCreation(account, initialDeposit);
    }

    private double minimumSavingsDeposit(Customer customer) {
        return (customer instanceof PremiumCustomer) ? PREMIUM_MIN_DEPOSIT : REGULAR_MIN_DEPOSIT;
    }

    private boolean persistNewAccount(AccountCreation creation) {
        Account account = creation.account();
        if (!accountManager.addAccount(account)) {
            return false;
        }
        Transaction transaction = new Transaction(
                account.getAccountNumber(),
                TransactionType.DEPOSIT.name(),
                creation.initialDeposit(),
                account.getBalance()
        );
        transactionManager.addTransaction(transaction);

        return true;
    }

    private Customer createCustomerFromData(CustomerData data) {

        print(" ");
        print("Customer type:");
        print("1. Regular Customer (Standard banking services)");
        print("2. Premium Customer (Enhanced benefits, min balance $10,000)");

        int customerType = getValidIntInput("Select type (1-2): ", 1, 2);

        if (customerType == 2) {
            return new PremiumCustomer(data.name, data.age, data.contact, data.address);
        } else {
            return new RegularCustomer(data.name, data.age, data.contact, data.address);
        }
    }

    public void viewAccountDetails() {
        print("\nVIEW ACCOUNT DETAILS");
        String accountNumber = readString("Enter account number (format: ACC###): ",
                isValidAccountNumber,
                "Error: Invalid account number format. Please use format ACC###"
        );

        if (!accountManager.accountExists(accountNumber)) {
            print("Error: Account not found. Please check the account number and try again.");
            pressEnterToContinue();
            return;
        }

        Account account = accountManager.getAccount(accountNumber);
        if (account != null) {
            displayAccountDetails(account);
        } else {
            print("\nError: Account not found. Please check the account number and try again.");
        }
        pressEnterToContinue();
    }
    public void viewAccountsByType() {
        print("Account Types ");
        print("1. Savings Account ");
        print("2. Checking Account ");
        int type = getValidIntInput("Select type (1-2): ", 1, 2);

        String accountType = type == 1 ? "savings" : "checking";

       List<Account> accounts = accountManager.getAccountsByType(accountType);
        // Sort accounts by account number (ascending)
        accounts.sort(Comparator.comparing(Account::getAccountNumber));
        if (accounts.isEmpty()) {
            print("No " + accountType + " accounts found.");
            return;
        }

        print(" ");
        printHeader( accountType.toUpperCase() + " Accounts");
        printSeparator();
        printf("%-8s | %-15s | %-9s | %-10s | %-8s%n",
                "ACC NO", "CUSTOMER NAME", "TYPE", "BALANCE", "STATUS");
        printSeparator();
        for (Account acct : accounts) {
            acct.displayAccountDetails();
            printSeparator();
        }
        if (accounts.isEmpty()) {
            print("No " + accountType + " accounts found.");
            return;
        }


        print(" ");
        printHeader( accountType.toUpperCase() + " Accounts");
        printSeparator();
        printf("%-8s | %-15s | %-9s | %-10s | %-8s%n",
                "ACC NO", "CUSTOMER NAME", "TYPE", "BALANCE", "STATUS");
        printSeparator();
        for (Account acct : accounts) {
            acct.displayAccountDetails();
            printSeparator();
        }

        printf("Total Accounts: %d%n", accounts.size());
        printf("Total Bank Balance: $%,.2f%n", accounts.stream().mapToDouble(Account::getBalance).sum());
        pressEnterToContinue(); // Wait for user to press Enter

    }
    public void deleteAccount() {

        String accountNumber = readString("Enter account number to delete (format: ACC###): ",
                isValidAccountNumber,
                "Error: Invalid account number format. Please use format ACC###"
        );
        if (!accountManager.accountExists(accountNumber)) {
            print("Error: Account not found. Please check the account number and try again.");
            pressEnterToContinue();
            return;
        }


        boolean success = accountManager.removeAccount(accountNumber);
        if (success) {
            print("Account deleted successfully!");
        } else {
            print("Account not found or could not be deleted.");
        }
        pressEnterToContinue();
    }
    public void updateCustomerInfo() {

        String accountNumber = readString("Enter account number to update (format: ACC###): ",
                isValidAccountNumber,
                "Error: Invalid account number format. Please use format ACC###"
        );
        if (!accountManager.accountExists(accountNumber)) {
            print("Error: Account not found. Please check the account number and try again.");
            pressEnterToContinue();
            return;
        }
        CustomerData data = readCustomerUpdateDetails();
        boolean success = accountManager.updateCustomerInfo(accountNumber, data.name, data.age, data.contact, data.address);
        if (success) {
            print("Customer information updated successfully!");
        } else {
            print("Failed to update customer information.");
        }
        pressEnterToContinue();
    }
    private CustomerData readCustomerUpdateDetails() {
        String name = readString("Enter New Customer Name: ",
                isValidName,
                "Error: Invalid name format. Name should contain only letters and spaces.");

        int age = getValidIntInput("Enter New customer age: ", 1, 150);

        String contact = readString("Enter New customer Phone Number: ",
                isValidPhone,
                "Error: Invalid phone format. Please enter a valid phone number.");

        String address = readString("Enter New customer address: ",
                isValidAddress,
                "Error: Invalid address format. Address should contain only letters, numbers, and spaces.");

        return new CustomerData(name, age, contact, address);
    }
    public void searchAccount() {

        String accountNumber = readString("Enter account number to search (format: ACC###): ",
                isValidAccountNumber,
                "Error: Invalid account number format. Please use format ACC###"
        );
        if (!accountManager.accountExists(accountNumber)) {
            print("Error: Account not found. Please check the account number and try again.");
            pressEnterToContinue();
            return;
        }

        Account account = accountManager.findAccount(accountNumber);
        if (account != null) {
            String accountType = account instanceof SavingsAccount ? "Savings" : "Checking";
            print("\nAccount Found for Account Number :" + account.getAccountNumber() + "\n");
            printf("Account Number: %s\n", account.getAccountNumber());
            printf("Account Type: %s\n", accountType);
            printf("Customer: %s\n", account.getCustomer().getName());
            printf("Age: %s\n", account.getCustomer().getAge());
            printf("Phone: %s\n", account.getCustomer().getContact());
            printf("Address: %s\n", account.getCustomer().getAddress());
            printf("Balance: $%,.2f\n", account.getBalance());
            printf("Account Status: %s\n", account.isActive() ? "Active" : "Inactive");

        } else {
            print("Account not found.");

        }
        pressEnterToContinue();
    }

    private void displayAccountDetails(Account account) {
        print("\nAccount Details for Account Number :" + account.getAccountNumber());
        print("Account Number: " + account.getAccountNumber());
        print("Account Type: " + account.getClass().getSimpleName());
        print("Customer Name: " + account.getCustomer().getName());
        print("Customer Age: " + account.getCustomer().getAge());
        print("Customer Contact: " + account.getCustomer().getContact());
        print("Customer Address: " + account.getCustomer().getAddress());
        print("Customer Type: " + account.getCustomer().getClass().getSimpleName());
        print("Current Balance: $" + String.format("%,.2f", account.getBalance()));
        print(Boolean.parseBoolean("Account Status: " + account.isActive()) ? "Active" : "Inactive");
        if (account instanceof SavingsAccount savings) {
            print("Minimum Balance: $" + String.format("%,.2f", savings.getMinimumBalance()));
        } else if (account instanceof CheckingAccount checking) {
            print("Overdraft Limit: $" + String.format("%.2f", checking.getOverdraftLimit()));
            print("Max Withdrawal Amount: $" + String.format("%.2f", checking.getMaxWithdrawalAmount()));
        }

        pressEnterToContinue();
    }


    public void listAllAccounts() {
       List<Account> savings = accountManager.getAccountsByType("savings");
    List<Account> checking = accountManager.getAccountsByType("checking");
    List<Account> all = new ArrayList<>();
    all.addAll(savings);
    all.addAll(checking);

    // Sort accounts by account number (ascending)
    all.sort(Comparator.comparing(Account::getAccountNumber));

    if (all.isEmpty()) {
        print("No accounts available.");
        pressEnterToContinue();
        return;
    }

    printHeader("ALL ACCOUNTS");
    printSeparator();
    printf("%-8s | %-15s | %-9s | %-10s | %-8s%n",
            "ACC NO", "CUSTOMER NAME", "TYPE", "BALANCE", "STATUS");
    printSeparator();
    for (Account acct : all) {
        acct.displayAccountDetails();
        printSeparator();
    }

    printf("Total Accounts: %d%n", all.size());
    printf("Total Bank Balance: $%,.2f%n", all.stream().mapToDouble(Account::getBalance).sum());
    pressEnterToContinue();
    }

    public void initializeSampleData() {
        Customer customer1 = new RegularCustomer("John Smith", 35, "+1-555-0101", "456 Elm Street, Metropolis");
        Customer customer2 = new RegularCustomer("Sarah Johnson", 28, "+1-555-0102", "789 Oak Avenue, Metropolis");
        Customer customer3 = new PremiumCustomer("Michael Chen", 42, "+1-555-0103", "321 Pine Road, Metropolis");
        Customer customer4 = new RegularCustomer("Emily Brown", 31, "+1-555-0104", "654 Maple Drive, Metropolis");
        Customer customer5 = new PremiumCustomer("David Wilson", 48, "+1-555-0105", "987 Cedar Lane, Metropolis");

        List<AccountCreation> samples = List.of(
                new AccountCreation(new SavingsAccount(customer1, 5250.00), 5250.00),
                new AccountCreation(new CheckingAccount(customer2, 3450.00), 3450.00),
                new AccountCreation(new SavingsAccount(customer3, 15750.00), 15750.00),
                new AccountCreation(new CheckingAccount(customer4, 890.00), 890.00),
                new AccountCreation(new SavingsAccount(customer5, 25300.00), 25300.00)
        );

        samples.forEach(this::persistNewAccount);
    }


}
