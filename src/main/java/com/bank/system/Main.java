// Java
package com.bank.system;

import com.bank.system.exceptions.InvalidAmountException;
import com.bank.system.processes.AccountProcessHandler;
import com.bank.system.processes.TransactionProcessHandler;
import com.bank.system.services.AccountManager;
import com.bank.system.services.StatementGenerator;
import com.bank.system.services.TransactionManager;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.io.PrintWriter;


import static com.bank.system.utils.ConsoleUtil.*;
import static com.bank.system.utils.ValidationUtils.*;

public class Main {

    private final TransactionManager transactionManager;
    private final AccountManager accountManager;
    private final AccountProcessHandler accountProcessHandler;
    private final TransactionProcessHandler transactionProcessHandler;
    private final StatementGenerator statementGenerator;

    private Main() {
        this.accountManager = new AccountManager();
        this.transactionManager = new TransactionManager(accountManager);
        this.accountProcessHandler = new AccountProcessHandler(accountManager, transactionManager);
        this.statementGenerator = new StatementGenerator(accountManager, transactionManager);
        this.transactionProcessHandler = new TransactionProcessHandler(accountManager, transactionManager, statementGenerator);

    }

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        displayWelcomeMessage();
        accountProcessHandler.initializeSampleData();
        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getValidIntInput("Enter your choice: ", 1, 5);
            running = processMenuChoice(choice);
        }
        shutdown();
    }

    private boolean processMenuChoice(int choice) {
        return switch (choice) {
            case 1 -> {
                manageAccounts();
                yield true;
            }
            case 2 -> {
                performTransactions();
                yield true;
            }
            case 3 -> {
                generateAccountStatements();
                yield true;
            }
            case 4 -> {
                runTests();
                yield true;
            }
            case 5 -> false;
            default -> true;
        };
    }
    private void manageAccounts() {
        boolean running = true;
        while (running) {
            print("\nMANAGE ACCOUNTS");
            print("1. Create Account");
            print("2. View Account Details");
            print("3. List All Accounts");
            print("4. Back to main");

            int choice = getValidIntInput("Choose an option:", 1, 4);
            running = processAccountMenuChoice(choice);
        }
        
    }
    private boolean processAccountMenuChoice(int choice) {
        return switch (choice) {
            case 1 -> {
                accountProcessHandler.createAccount();
                yield true;
            }
            case 2 -> {
                accountProcessHandler.viewAccountDetails();
                yield true;
            }
            case 3 -> {
                accountProcessHandler.listAllAccounts();
                yield true;
            }
            case 4 -> false;

            default -> throw new IllegalStateException("Unexpected value: " + choice);
        };
    }

    private void performTransactions() {
        print(" ");
        print("PROCESS TRANSACTION");
        print(subSeparator(60));
        print(" ");

        String accountNumber = readString("Enter Account Number: ",
                isValidAccountNumber,
                "Error: Invalid account number format. Please use format ACC###"
        );

        if (!accountManager.accountExists(accountNumber)) {
            print("Error: Account not found. Please check the account number and try again.");
            pressEnterToContinue();
            return;
        }
        boolean running = true;
        while (running) {
            print("Select transaction type:");
            print("1. Deposit");
            print("2. Withdrawal");
            print("3. Transfer");
            print("4. View Transaction History");
            print("5. Back to Main Menu");

            int choice = getValidIntInput("Choose an option:", 1, 5);
            running = processTransactionMenuChoice(choice, accountNumber);

        }

    }
     private boolean processTransactionMenuChoice(int choice, String accountNumber) {
         try {
             return switch (choice) {
                 case 1 -> {
                     transactionProcessHandler.performDeposit(accountNumber);
                     yield true;
                 }
                 case 2 -> {
                     transactionProcessHandler.performWithdrawal(accountNumber);
                     yield true;
                 }
                 case 3 -> {
                     transactionProcessHandler.performTransfer(accountNumber);
                     yield true;
                 }
                 case 4 -> {
                     transactionProcessHandler.viewTransactionHistory(accountNumber);
                     yield true;
                 }
                 case 5 -> false;
                 default -> throw new IllegalArgumentException("Unexpected value: " + choice);
             };
         }  catch (Exception e) {
             print("Transaction Failed: " + e.getMessage());
             return false;
         }
     }
    private void generateAccountStatements() {
        print("\nGENERATE ACCOUNT STATEMENT");
        String accountNumber = readString("Enter Account Number: ",
                isValidAccountNumber,
                "Error: Invalid account number format. Please use format ACC###"
        );

        if (!accountManager.accountExists(accountNumber)) {
            print("Error: Account not found. Please check the account number and try again.");
            pressEnterToContinue();
            return;
        }

        String statement = statementGenerator.generateStatement(accountNumber);
        print("\n" + statement);
        pressEnterToContinue();
    }

    public static void runTests() {
        boolean running = true;
        while (running) {
            print("\nRUN TESTS");
            print("1. Run all tests");
            print("2. Run AccountTest");
            print("3. Run TransactionTest");
            print("4. Run ExceptionTest");
            print("5. Back to Main Menu");

            int selection = getValidIntInput("Choose an option: ", 1, 5);


            if (selection == 5){
                running = false;
            }
            LauncherDiscoveryRequest request = buildDiscoveryRequest(selection);
            if (request == null) {
                print("Invalid selection. Returning to main menu.");
                pressEnterToContinue();
            }
            SummaryGeneratingListener listener = new SummaryGeneratingListener();
            Launcher launcher = LauncherFactory.create();
            print("\nExecuting tests...\n");
            launcher.execute(request, listener);

            var summary = listener.getSummary();
            summary.printTo(new PrintWriter(System.out));
            if (summary.getTotalFailureCount() == 0) {
                print("\n✓ Tests completed successfully.");
            } else {
                print("\n✗ Tests completed with failures.");
            }
            pressEnterToContinue();


        }

    }


    private static LauncherDiscoveryRequest buildDiscoveryRequest(int selection) {
        LauncherDiscoveryRequestBuilder builder = LauncherDiscoveryRequestBuilder.request();
        return switch (selection) {
            case 1 -> builder.selectors(DiscoverySelectors.selectPackage("com.bank.system.test"))
                    .build();
            case 2 -> builder.selectors(DiscoverySelectors.selectClass(
                                "com.bank.system.test.AccountTest"))
                        .build();
            case 3 -> builder.selectors(DiscoverySelectors.selectClass(
                            "com.bank.system.test.TransactionManagerTest"))
                    .build();
            case 4 -> builder.selectors(DiscoverySelectors.selectClass(
                            "com.bank.system.test.ExceptionTest"))
                    .build();
            default -> null;
        };
    }


    public void displayWelcomeMessage() {
        print("\nWelcome to the Bank Account Management System!");
        print("Please select an option from the menu below:");
    }

    private void displayMainMenu() {
        printHeader("BANK ACCOUNT MANAGEMENT SYSTEM - MAIN MENU");
        print("BANK ACCOUNT MANAGEMENT - MAIN MENU");
        print(" ");
        print("1. Manage Accounts");
        print("2. Perform Transactions");
        print("3. Generate Account Statements");
        print("4. Run Tests");
        print("5. Exit");
        print("");
    }

    private void shutdown() {
        print("\nThank you for using Bank Account Management System!");
        print("All data saved in memory. Remember to commit your latest changes to Git!");
        print("Goodbye!");
    }
}
