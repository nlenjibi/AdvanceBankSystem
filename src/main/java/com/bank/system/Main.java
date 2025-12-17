// Java
package com.bank.system;

import com.bank.system.models.Account;

import com.bank.system.models.Transaction;
import com.bank.system.processes.AccountProcessHandler;
import com.bank.system.processes.TransactionProcessHandler;
import com.bank.system.services.AccountManager;
import com.bank.system.services.StatementGenerator;
import com.bank.system.services.TransactionManager;
import com.bank.system.utils.ConcurrencyUtils;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import com.bank.system.services.FilePersistence;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;


import static com.bank.system.utils.ConsoleUtil.*;
import static com.bank.system.utils.ValidationUtils.*;

public class Main {

    private static final AccountManager accountManager = new AccountManager();
    private  static final TransactionManager transactionManager = new TransactionManager(accountManager);

    private final AccountProcessHandler accountProcessHandler;
    private final TransactionProcessHandler transactionProcessHandler;
    private final StatementGenerator statementGenerator;
    private static final FilePersistence filePersistence  = new FilePersistence();;

    private Main() {
        this.accountProcessHandler = new AccountProcessHandler(accountManager, transactionManager);
        this.statementGenerator = new StatementGenerator(accountManager, transactionManager);
        this.transactionProcessHandler = new TransactionProcessHandler(accountManager, transactionManager, statementGenerator);
    }

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        displayWelcomeMessage();
        //accountProcessHandler.initializeSampleData();
        loadDataFromFiles();

        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getValidIntInput("Enter your choice: ", 1, 7);
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
                saveLoadData();
                yield true;
            }case 5 -> {
                runConcurrentSimulation();
                yield true;
            }case 6 -> {
                runTests();
                yield true;
            }
            case 7 -> false;
            default -> true;
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
        print("4. Save/Load Data");
        print("5. Run Concurrent Simulation");
        print("6. Run Tests");
        print("7. Exit");
        print("");
    }
    private void manageAccounts() {
        boolean running = true;
        while (running) {
            print("\nMANAGE ACCOUNTS");
            print("1. Create Account");
            print("2. View Account Details");
            print("3. View All Accounts");
            print("4. Search Account");
            print("5. Update Customer Info");
            print("6. Delete Account");
            print("7. View Accounts by Type");
            print("8. Back to Main Menu");


            int choice = getValidIntInput("Choose an option:", 1, 8);
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
            }case 4 -> {
                accountProcessHandler.searchAccount();
                yield true;
            }case 5 -> {
                accountProcessHandler.updateCustomerInfo();
                yield true;
            }case 6 -> {
                accountProcessHandler.deleteAccount();
                yield true;
            }case 7 -> {
                accountProcessHandler.viewAccountsByType();
                yield true;
            }
            case 8 -> false;

            default -> throw new IllegalStateException("Unexpected value: " + choice);
        };
    }

    private void performTransactions() {
        print(" ");
        print("PROCESS TRANSACTION");
        print(subSeparator(60));
        print(" ");

        String accountNumber = readString("Enter account number (format: ACC###): ",
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
        String accountNumber = readString("Enter account number (format: ACC###): ",
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
    private static void saveLoadData() {
        boolean backToMain = false;

        while (!backToMain) {
            print("\n--- Save/Load Data ---");
            print("1. Save All Data to Files");
            print("2. Load Data from Files");
            print("3. Back to Main Menu");
            int choice = getValidIntInput("Choose an option:", 1, 3);

            switch (choice) {
                case 1:
                    saveDataToFiles();
                    break;
                case 2:
                    loadDataFromFiles();
                    break;
                case 3:
                    backToMain = true;
                    break;
                default:
                    print("Invalid choice. Please try again.");
            }
        }
    }
    private static void loadDataFromFiles() {

        Map<String, Account> loadedAccounts = filePersistence.loadAccounts();

        if (loadedAccounts != null && !loadedAccounts.isEmpty()) {
            accountManager.getAccountsMap().putAll(loadedAccounts);
            print("Loaded " + loadedAccounts.size() + " accounts.");
        } else {
            print("No account data found to load.");
        }

        List<Transaction> loadedTransactions = filePersistence.loadTransactions();
        if (loadedTransactions != null && !loadedTransactions.isEmpty()) {
            transactionManager.setTransactions(loadedTransactions);
            print("Loaded " + loadedTransactions.size() + " transactions.");
        } else {
            print("No transactions found to load.");
        }
        pressEnterToContinue();
    }
    private static void runConcurrentSimulation() {

        boolean backToMain = false;
        while (!backToMain) {
            print("Simulation Menu");
            print("1. Run Single Simulation");
            print("2. Run Multi Account Simulation");
            print("3. Back to Main Menu");
            int choice = getValidIntInput("Choose an option:", 1, 3);

            switch (choice) {
                case 1:
                    ConcurrencyUtils.runConcurrentSimulation(accountManager, transactionManager, 5);
                    break;
                case 2:
                    ConcurrencyUtils.runMultiAccountConcurrentSimulation(accountManager, transactionManager, 5);
                    break;
                case 3:
                    backToMain = true;
                    break;
                default:
                    print("Invalid choice. Please try again.");
            }
        }




    }

    private static void saveDataToFiles() {
        print("\nSAVING ACCOUNT DATA");
        filePersistence.saveAccounts(accountManager.getAccountsMap());
        filePersistence.saveTransactions(transactionManager.getAllTransactions());
        System.out.println("File save completed successfully.");
        pressEnterToContinue();
    }

    private void shutdown() {
        // Save data before exiting
        saveDataToFiles();
        print("\nThank you for using Bank Account Management System!");
        print("Data automatically saved to disk.");
        print("Goodbye!");
    }

}
