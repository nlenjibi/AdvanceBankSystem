package com.bank.system.processes;


import com.bank.system.exceptions.InvalidAmountException;
import com.bank.system.models.Account;
import com.bank.system.models.SavingsAccount;
import com.bank.system.models.Transaction;
import com.bank.system.services.AccountManager;
import com.bank.system.services.StatementGenerator;
import com.bank.system.services.TransactionManager;


import static com.bank.system.utils.ConsoleUtil.getValidDoubleInput;
import static com.bank.system.utils.ConsoleUtil.print;
import static com.bank.system.utils.ConsoleUtil.pressEnterToContinue;
import static com.bank.system.utils.ConsoleUtil.readConfirmation;
import static com.bank.system.utils.ConsoleUtil.readString;
import static com.bank.system.utils.ValidationUtils.isValidAccountNumber;
import static com.bank.system.utils.ValidationUtils.isValidAmount;


public class TransactionProcessHandler {
    private final TransactionManager transactionManager;
    private final AccountManager accountManager;
    private final StatementGenerator statementGenerator;
    private static final String AMOUNT_MUST_BE_POSITIVE = "Amount must be greater than zero.";
    private static final String TRANS_DETAILS_UNAVAILABLE = "Transaction details unavailable.";

    public TransactionProcessHandler(AccountManager accountManager, TransactionManager transactionManager, StatementGenerator statementGenerator) {
        this.transactionManager = transactionManager;
        this.accountManager = accountManager;
        this.statementGenerator = statementGenerator;
    }
    public void performDeposit(String accountNumber) throws InvalidAmountException {


        double amount = getValidDoubleInput("Enter amount to deposit: $",
                isValidAmount,
                AMOUNT_MUST_BE_POSITIVE);

        Account account = accountManager.getAccount(accountNumber);
        double previousBalance = account.getBalance();
        boolean success = transactionManager.deposit(accountNumber, amount);

        if (!success) {
            print("Deposit failed. Invalid amount.");
            pressEnterToContinue();
            return;
        }

        Transaction transaction = transactionManager.getLastTransaction(accountNumber);

        if (transaction != null) {
            transaction.displayTransactionDetails(previousBalance);
        } else {
            print(TRANS_DETAILS_UNAVAILABLE);
        }
        print(" ");
        boolean confirmed = readConfirmation();

        handleTransactionConfirmation(confirmed, account, previousBalance, captureTransactions(accountNumber));
        pressEnterToContinue();


    }


    public void performWithdrawal(String accountNumber) throws InvalidAmountException {
        double amount = getValidDoubleInput("Enter amount to withdraw: $",
                isValidAmount,AMOUNT_MUST_BE_POSITIVE);

        Account account = accountManager.getAccount(accountNumber);
        double previousBalance = account.getBalance();

        boolean success = transactionManager.withdraw(accountNumber, amount);

        if (!success) {
            if (account instanceof SavingsAccount) {
                print("Withdrawal failed. Insufficient funds.");
            } else {
                print("Withdrawal failed. Insufficient funds or exceeds overdraft limit.");
            }
            pressEnterToContinue();
            return;
        }
        Transaction transaction = transactionManager.getLastTransaction(accountNumber);

        if (transaction != null) {
            transaction.displayTransactionDetails(previousBalance);
        } else {
            print(TRANS_DETAILS_UNAVAILABLE);
        }
        print(" ");
        boolean confirmed = readConfirmation();
        handleTransactionConfirmation(confirmed, account, previousBalance, captureTransactions(accountNumber));
        pressEnterToContinue();

    }

    public void performTransfer(String fromAccountNumber) throws InvalidAmountException {
        String toAccountNumber = readString("Enter destination account number (format: ACC###): ",
                isValidAccountNumber,
                "Error: Invalid account number format. Please use format ACC###"
        );

        if (!accountManager.accountExists(toAccountNumber)) {
            print("Error: Destination account not found. Please check the account number and try again.");
            pressEnterToContinue();
            return;
        }

        double amount = getValidDoubleInput("Enter amount to transfer: $",
                isValidAmount,
                AMOUNT_MUST_BE_POSITIVE);


        Account fromAccount = accountManager.getAccount(fromAccountNumber);
        Account toAccount = accountManager.getAccount(toAccountNumber);

        if(fromAccount.equals(toAccount)) {
            print("Error: Source and destination accounts cannot be the same.");
            pressEnterToContinue();
            return;
        }

        double fromPreviousBalance = fromAccount.getBalance();
        double toPreviousBalance = toAccount.getBalance();

        transactionManager.transfer(fromAccountNumber, toAccountNumber, amount);
        Transaction fromTransaction = transactionManager.getLastTransaction(fromAccountNumber);

        if (fromTransaction != null) {
            fromTransaction.displayTransactionDetails(fromPreviousBalance);
        } else {
            print(TRANS_DETAILS_UNAVAILABLE);
        }
        print(" ");
        boolean confirmed = readConfirmation();
        if (confirmed) {
            print("\n✓ Transfer successful!");
            print("From Account: " + fromAccountNumber + " (Previous: $" + String.format("%.2f", fromPreviousBalance) +
                    ", New: $" + String.format("%.2f", fromAccount.getBalance()) + ")");
            print("To Account: " + toAccountNumber + " (Previous: $" + String.format("%.2f", toPreviousBalance) +
                    ", New: $" + String.format("%.2f", toAccount.getBalance()) + ")");
            print("Transfer Amount: $" + String.format("%.2f", amount));
        }
        else {
            rollbackTransactions(captureTransactions(fromAccountNumber), captureTransactions(toAccountNumber), fromAccount, toAccount, fromPreviousBalance, toPreviousBalance);
            fromAccount.setBalance(fromPreviousBalance);
            toAccount.setBalance(toPreviousBalance);
            print(" ");
            print("Transaction cancelled.");


        }
    }
    public void viewTransactionHistory(String accountNumber) {
        String statement = statementGenerator.generateStatement(accountNumber);
        print("\n" + statement);
        pressEnterToContinue();
    }



    private void handleTransactionConfirmation(boolean confirmed, Account account, double previousBalance, TransactionSnapshot snapshot) {
        if (confirmed) {
            print(" ");
            print("✓ Transaction completed successfully!");
        } else {
            rollbackTransactions(snapshot, account, previousBalance);
            print(" ");
            print("Transaction cancelled.");
        }
        pressEnterToContinue();

    }


    private TransactionSnapshot captureTransactions(String accountNumber) {
        TransactionSnapshot snapshot = new TransactionSnapshot(accountNumber);
        snapshot.capture(transactionManager.getTransactionsForAccount(accountNumber));
        return snapshot;
    }

    private void rollbackTransactions(TransactionSnapshot snapshot, Account account, double previousBalance) {
        if (snapshot == null) {
            return;
        }
        snapshot.removeFrom(transactionManager);
        account.setBalance(previousBalance);
    }

    private void rollbackTransactions(TransactionSnapshot fromSnapshot, TransactionSnapshot toSnapshot,
                                      Account fromAccount, Account toAccount,
                                      double fromBalance, double toBalance) {
        rollbackTransactions(fromSnapshot, fromAccount, fromBalance);
        rollbackTransactions(toSnapshot, toAccount, toBalance);
    }

    private static final class TransactionSnapshot {
        private final String accountNumber;
        private String latestTransactionId;

        private TransactionSnapshot(String accountNumber) {
            this.accountNumber = accountNumber;
        }

        private void capture(java.util.List<Transaction> transactions) {
            if (transactions == null || transactions.isEmpty()) {
                return;
            }
            Transaction latest = transactions.getLast();
            if (latest != null && accountNumber.equals(latest.getAccountNumber())) {
                this.latestTransactionId = latest.getTransactionId();
            }
        }

        private void removeFrom(TransactionManager manager) {
            if (latestTransactionId == null) {
                return;
            }
            manager.removeTransaction(latestTransactionId);

        }
    }

}
