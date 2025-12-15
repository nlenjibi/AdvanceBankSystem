package com.bank.system.services;

import com.bank.system.models.Account;
import com.bank.system.models.Transaction;

import java.util.ArrayList;
import java.util.List;
import static com.bank.system.utils.ConsoleUtil.subSeparator;
import static com.bank.system.utils.ConsoleUtil.separator;

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

            List<Transaction> sortedTransactions = transactionManager.sortTransactionsByTimestampDesc(transactions);
            for (Transaction transaction : sortedTransactions) {
                String sign = transactionManager.isCreditTransaction(transaction) ? "+" : "-";
                statement.append(String.format("%-12s | %-20s | %-12s | %s$%,12.2f | $%,15.2f%n",
                        transaction.getTransactionId(),
                        transaction.getTimestamp(),
                        transaction.getType(),
                        sign,
                        transaction.getAmount(),
                        transaction.getBalanceAfter()));
            }

            double netChange = transactionManager.getTotalDeposits(accountNumber) - transactionManager.getTotalWithdrawals(accountNumber);
            statement.append(subSeparator(90)).append("\n\n");
            statement.append("SUMMARY:\n");
            statement.append(subSeparator(35)).append("\n");
            statement.append("Total Transactions: ").append(transactions.size()).append("\n");
            statement.append(String.format("Total Deposits: $%,.2f%n", transactionManager.getTotalDeposits(accountNumber)));
            statement.append(String.format("Total Withdrawals: $%,.2f%n", transactionManager.getTotalWithdrawals(accountNumber)));
            statement.append(String.format("Total Received: $%,.2f%n", transactionManager.getTotalReceived(accountNumber)));
            statement.append(String.format("Total Sent: $%,.2f%n", transactionManager.getTotalTranfer(accountNumber)));
            statement.append(String.format("Net Change: %s$%,.2f%n",
                    netChange >= 0 ? "+" : "-",
                    Math.abs(netChange)));
        }
        
        statement.append("\n✓ Statement generated successfully.");
        return statement.toString();
    }




}