package com.bank.system.services;

import com.bank.system.enums.TransactionType;
import com.bank.system.exceptions.InvalidAmountException;
import com.bank.system.models.Account;
import com.bank.system.models.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionManager {
    private final List<Transaction> allTransactions;
    private final AccountManager accountManager;

    public TransactionManager(AccountManager accountManager) {
        this.accountManager = accountManager;
        this.allTransactions = Collections.synchronizedList(new ArrayList<>());
    }



    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            return;
        }

        allTransactions.add(transaction);
    }


    public int getTransactionCount() {
        return allTransactions.size();
    }

    public  boolean deposit(String accountNumber, double amount) throws InvalidAmountException {
        Account account = fetchAccount(accountNumber);
        validateAmount(amount, "Deposit");
        boolean success = account.processTransaction(amount, TransactionType.DEPOSIT);
        if (success) {
            recordTransaction(account, TransactionType.DEPOSIT, amount);
        }
        return success;
    }

    public  boolean withdraw(String accountNumber, double amount) throws InvalidAmountException {
        Account account = fetchAccount(accountNumber);
        validateAmount(amount, "Withdrawal");
        boolean success = account.processTransaction(amount, TransactionType.WITHDRAWAL);
        if (success) {
            recordTransaction(account, TransactionType.WITHDRAWAL, amount);
        }
        return success;
    }

    public boolean transfer(String fromAccountNumber, String toAccountNumber, double amount)
            throws InvalidAmountException {
        if (fromAccountNumber == null || toAccountNumber == null) {
            throw new IllegalArgumentException("Account numbers must not be null");
        }
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        validateAmount(amount, "Transfer");
        Account fromAccount = fetchAccount(fromAccountNumber);
        Account toAccount = fetchAccount(toAccountNumber);

        Account firstLock = fromAccount;
        Account secondLock = toAccount;
        if (fromAccount.getAccountNumber().compareTo(toAccount.getAccountNumber()) > 0) {
            firstLock = toAccount;
            secondLock = fromAccount;
        }

        synchronized (firstLock) {
            synchronized (secondLock) {
                boolean withdrawalSuccess = fromAccount.processTransaction(amount, TransactionType.WITHDRAWAL);
                if (!withdrawalSuccess) {
                    return false;
                }

                boolean depositSuccess = toAccount.processTransaction(amount, TransactionType.DEPOSIT);
                if (!depositSuccess) {
                    fromAccount.processTransaction(amount, TransactionType.DEPOSIT);
                    return false;
                }

                recordTransaction(fromAccount, TransactionType.TRANSFER, amount);
                recordTransaction(toAccount, TransactionType.RECEIVE, amount);
                return true;
            }
        }
    }

    public List<Transaction> getTransactionsForAccount(String accountNumber) {
        return allTransactions.stream()
                .filter(t -> isMatchingAccount(t, accountNumber))
                .collect(Collectors.toList());
    }

    public List<Transaction> getAllTransactions() {
        synchronized (allTransactions) {
            return new ArrayList<>(allTransactions);
        }
    }

    public int getTotalTransactions() {
        return allTransactions.size();
    }

    public void removeTransaction(String transactionId) {
        allTransactions.removeIf(transaction -> transaction.getTransactionId().equals(transactionId));
    }

    public Transaction getLastTransaction(String accountNumber) {
        synchronized (allTransactions) {
            for (int i = allTransactions.size() - 1; i >= 0; i--) {
                Transaction transaction = allTransactions.get(i);
                if (isMatchingAccount(transaction, accountNumber)) {
                    return transaction;
                }
            }
        }
        return null;
    }

    private boolean isMatchingAccount(Transaction transaction, String accountNumber) {
        return transaction != null && accountNumber.equals(transaction.getAccountNumber());
    }

    private void recordTransaction(Account account, TransactionType type, double amount) {
        Transaction transaction = createTransaction(account.getAccountNumber(), type, amount, account.getBalance());
        allTransactions.add(transaction);
    }

    private Transaction createTransaction(String accountNumber, TransactionType type, double amount, double balanceAfter) {
        return new Transaction(accountNumber, type.name(), amount, balanceAfter);
    }

    public double getTotalDeposits(String accountNumber) {
        if (accountNumber == null) {
            return 0.0;
        }
        return allTransactions.stream()
                .filter(t -> isMatchingAccount(t, accountNumber))
                .filter(t -> "DEPOSIT".equalsIgnoreCase(t.getType()) )
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
    public double getTotalReceived(String accountNumber) {
        if (accountNumber == null) {
            return 0.0;
        }
        return allTransactions.stream()
                .filter(t -> isMatchingAccount(t, accountNumber))
                .filter(t -> "RECEIVE".equalsIgnoreCase(t.getType()) )
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
  public double getTotalWithdrawals(String accountNumber) {
      if (accountNumber == null) {
          return 0.0;
      }
      return allTransactions.stream()
              .filter(t -> isMatchingAccount(t, accountNumber))
              .filter(t -> "WITHDRAWAL".equalsIgnoreCase(t.getType()) || "TRANSFER".equalsIgnoreCase(t.getType()))
              .mapToDouble(Transaction::getAmount)
              .sum();
  }
    public double getTotalTranfer(String accountNumber) {
        if (accountNumber == null) {
            return 0.0;
        }
        return allTransactions.stream()
                .filter(t -> isMatchingAccount(t, accountNumber))
                .filter(t -> "TRANSFER".equalsIgnoreCase(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    private void validateAmount(double amount, String context) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException(context + " amount must be greater than 0");
        }
    }

    private Account fetchAccount(String accountNumber) throws InvalidAmountException {
        Account account = accountManager.getAccount(accountNumber);
        if (account == null) {
            throw new InvalidAmountException("Account not found: " + accountNumber);
        }
        return account;
    }

    public void setTransactions(List<Transaction> transactions) {
        synchronized (allTransactions) {
            allTransactions.clear();
            if (transactions != null) {
                allTransactions.addAll(transactions);
            }
        }
    }
    public List<Transaction> sortTransactionsByTimestampDesc(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return Collections.emptyList();
        }
        return transactions.stream()
                .sorted(java.util.Comparator.comparing(Transaction::getTimestamp,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                        .reversed())
                .collect(Collectors.toList());
    }

    public boolean isCreditTransaction(Transaction transaction) {
        if (transaction == null) {
            return false;
        }
        String type = transaction.getType();
        return type != null && ("DEPOSIT".equalsIgnoreCase(type) || "RECEIVE".equalsIgnoreCase(type));
    }


}
