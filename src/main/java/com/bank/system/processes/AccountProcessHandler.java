package com.bank.system.processes;


import com.bank.system.enums.TransactionType;
import com.bank.system.models.*;
import com.bank.system.models.Transaction;
import com.bank.system.services.*;

import java.util.List;

import static com.bank.system.utils.ConsoleUtil.*;
import static com.bank.system.utils.ValidationUtils.*;


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

        if (accountManager.accountExists(accountNumber)) {
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

    private void displayAccountDetails(Account account) {
        print("\nAccount Details:");
        print("Account Number: " + account.getAccountNumber());
        print("Account Type: " + account.getClass().getSimpleName());
        print("Customer: " + account.getCustomer().getName());
        print("Customer Type: " + account.getCustomer().getClass().getSimpleName());
        print("Current Balance: $" + String.format("%.2f", account.getBalance()));

        if (account instanceof SavingsAccount savings) {
            print("Minimum Balance: $" + String.format("%.2f", savings.getMinimumBalance()));
        } else if (account instanceof CheckingAccount checking) {
            print("Overdraft Limit: $" + String.format("%.2f", checking.getOverdraftLimit()));
            print("Max Withdrawal Amount: $" + String.format("%.2f", checking.getMaxWithdrawalAmount()));
        }
    }

    public void listAllAccounts() {
       accountManager.viewAllAccounts();
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
