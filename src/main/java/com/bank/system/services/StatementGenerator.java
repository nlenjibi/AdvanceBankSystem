package com.bank.system.services;

import com.bank.system.models.*;

import java.util.ArrayList;
import java.util.List;
import static com.bank.system.utils.ConsoleUtil.*;

public class StatementGenerator {
    private final AccountManager accountManager;
    private final TransactionManager transactionManager;
    
    public StatementGenerator(AccountManager accountManager, TransactionManager transactionManager) {
        this.accountManager = accountManager;
        this.transactionManager = transactionManager;
    }
    
    public String generateStatement(String accountNumber) {
        Account account = accountManager.getAccount(accountNumber);
        if (account == null) {
            return "Error: Account not found. Please check the account number and try again.";
        }
        
        List<Transaction> transactions = transactionManager.getTransactionsForAccount(accountNumber);
        StatementTotals totals = calculateTotals(transactions);


        StringBuilder statement = new StringBuilder();
        statement.append(" \n");
        statement.append("TRANSACTION HISTORY FOR ACCOUNT: ")
                .append(account.getAccountNumber())
                .append(" - ")
                .append(account.getCustomer().getName())
                .append("\n");
        statement.append("============================================================\n");
        statement.append(" \n");
        statement.append("Account: ")
                .append(account.getAccountNumber())
                .append(" - ")
                .append(account.getCustomer().getName())
                .append("\n");
        statement.append("Account Type: ")
                .append(account.getClass().getSimpleName())
                .append("\n");
        statement.append(String.format("Current Balance: $%,.2f%n%n", account.getBalance()));

        if (transactions.isEmpty()) {
            statement.append(separator(50)).append("\n");
            statement.append("No transactions found for this account.\n");
            statement.append(separator(50)).append("\n");
        } else {
            statement.append("TRANSACTION HISTORY\n");
            statement.append(subSeparator(90)).append("\n");
            statement.append(String.format("%-12s | %-20s | %-12s | %-14s | %-15s%n",
                    "TXN ID", "DATE/TIME", "TYPE", "AMOUNT", "BALANCE AFTER"));
            statement.append(subSeparator(90)).append("\n");

            List<Transaction> sortedTransactions = sortTransactionsByTimestampDesc(transactions);
            for (Transaction transaction : sortedTransactions) {
                String sign = isCreditTransaction(transaction) ? "+" : "-";
                statement.append(String.format("%-12s | %-20s | %-12s | %s$%,12.2f | $%,15.2f%n",
                        transaction.getTransactionId(),
                        transaction.getTimestamp(),
                        transaction.getType(),
                        sign,
                        transaction.getAmount(),
                        transaction.getBalanceAfter()));
            }

            double netChange = totals.totalDeposits - totals.totalWithdrawals;
            statement.append(subSeparator(90)).append("\n\n");
            statement.append("SUMMARY:\n");
            statement.append(subSeparator(35)).append("\n");
            statement.append("Total Transactions: ").append(transactions.size()).append("\n");
            statement.append(String.format("Total Deposits: $%,.2f%n", totals.totalDeposits));
            statement.append(String.format("Total Withdrawals: $%,.2f%n", totals.totalWithdrawals));
            statement.append(String.format("Total Received: $%,.2f%n", totals.totalReceived));
            statement.append(String.format("Total Sent: $%,.2f%n", totals.totalSent));
            statement.append(String.format("Net Change: %s$%,.2f%n",
                    netChange >= 0 ? "+" : "-",
                    Math.abs(netChange)));
        }
        
        statement.append("\n✓ Statement generated successfully.");
        return statement.toString();
    }

    private List<Transaction> sortTransactionsByTimestampDesc(List<Transaction> transactions) {
        List<Transaction> sortedTransactions = new ArrayList<>(transactions);
        sortedTransactions.sort((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()));
        return sortedTransactions;
    }

    private boolean isCreditTransaction(Transaction transaction) {
        String type = transaction.getType();
        return "DEPOSIT".equalsIgnoreCase(type) || "RECEIVE".equalsIgnoreCase(type);
    }

    private StatementTotals calculateTotals(List<Transaction> transactions) {
        StatementTotals totals = new StatementTotals();
        for (Transaction transaction : transactions) {
            if (transaction == null) {
                continue;
            }
            String type = transaction.getType();
            double amount = transaction.getAmount();
            if ("DEPOSIT".equalsIgnoreCase(type)) {
                totals.totalDeposits += amount;
            } else if ("WITHDRAWAL".equalsIgnoreCase(type)) {
                totals.totalWithdrawals += amount;
            } else if ("RECEIVE".equalsIgnoreCase(type)) {
                totals.totalReceived += amount;
            } else if ("TRANSFER".equalsIgnoreCase(type)) {
                totals.totalSent += amount;
            }
        }
        return totals;
    }

    private String formatAmount(double amount) {
        return String.format("%.2f", amount);
    }

    private static final class StatementTotals {
        private double totalDeposits;
        private double totalWithdrawals;
        private double totalReceived;
        private double totalSent;
    }
}