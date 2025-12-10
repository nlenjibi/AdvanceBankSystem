# Collections Architecture Documentation

## Overview

This document details the implementation of Java Collections Framework in the Bank Account Management System, highlighting the usage of various collection types and functional programming patterns.

## Collections Used

### 1. HashMap for Account Storage

**Location**: `services/AccountManager.java`
**Purpose**: Store accounts with account number as key for O(1) lookup
**Thread Safety**: Implemented with `ConcurrentHashMap`

```java
private Map<String, Account> accounts = new ConcurrentHashMap<>();
```

**Benefits**:
- Fast retrieval of accounts by account number
- Efficient storage and management of account data
- Thread-safe operations for concurrent access

### 2. ArrayList for Transaction Storage

**Location**: `services/TransactionManager.java`
**Purpose**: Store transaction history with ordered access
**Thread Safety**: Wrapped with `Collections.synchronizedList`

```java
private List<Transaction> transactions = Collections.synchronizedList(new ArrayList<>());
```

**Benefits**:
- Maintains order of transactions
- Efficient for sequential access and iteration
- Thread-safe for concurrent transaction processing

### 3. ArrayList for Customer Transactions

**Location**: `models/Account.java`
**Purpose**: Store individual account transaction history
**Thread Safety**: Not required as it's managed within account context

```java
protected List<Transaction> transactions = new ArrayList<>();
```

## Functional Programming Implementation

### 1. Stream API Usage

#### Filtering Accounts
```java
public List<Account> searchAccounts(Predicate<Account> condition) {
    return accounts.values().stream()
            .filter(condition)
            .collect(Collectors.toList());
}
```

#### Sorting Transactions by Amount
```java
public List<Transaction> getTransactionsSortedByAmount() {
    return transactions.stream()
            .sorted((t1, t2) -> Double.compare(t2.getAmount(), t1.getAmount()))
            .collect(Collectors.toList());
}
```

#### Calculating Totals
```java
public double getTotalBalance() {
    return accounts.values().stream()
            .mapToDouble(Account::getBalance)
            .sum();
}
```

### 2. Lambda Expressions

#### Validation Predicates in `ValidationUtils`
```java
public static final Predicate<String> isValidAccountNumber = 
    accountNumber -> accountNumber != null && accountNumberRegex.matcher(accountNumber).matches();
```

### 3. Method References

#### For-each operations
```java
accounts.forEach(account -> {
    String accountType = account instanceof SavingsAccount ? "Savings" : "Checking";
    System.out.printf("Account: %s | Type: %s | Balance: $%.2f | Customer: %s\n",
            account.getAccountNumber(), accountType, account.getBalance(), account.getCustomer().getName());
});
```

## Concurrency and Thread Safety

### 1. Synchronized Collections
- `ConcurrentHashMap` in `AccountManager` for thread-safe account storage
- `Collections.synchronizedList` in `TransactionManager` for thread-safe transaction storage

### 2. Synchronized Methods
- Critical operations in `AccountManager` and `TransactionManager` are synchronized
- Prevents race conditions during concurrent access

### 3. Atomic Operations
- `AtomicInteger` used for generating unique transaction IDs
- Ensures thread-safe increment operations

## File I/O with Collections

### 1. Loading Data
```java
List<String> lines = Files.readAllLines(path);
// Process with streams to convert to objects
```

### 2. Saving Data
```java
List<String> lines = transactions.stream()
    .map(transaction -> String.format("%s|%s|%s|%s|%s|%s", ...))
    .collect(Collectors.toList());
Files.write(path, lines);
```

## Performance Considerations

1. **HashMap** for O(1) account retrieval
2. **ArrayList** for efficient sequential transaction processing
3. **Stream API** for functional data processing
4. **Concurrent Collections** for thread-safe operations
5. **Lazy Evaluation** in streams for memory efficiency

## Design Patterns

1. **Strategy Pattern** with functional interfaces for validation
2. **Observer Pattern** with collections for transaction tracking
3. **Repository Pattern** with collections for data management

## Benefits of Collections Implementation

- **Scalability**: Collections grow dynamically with data
- **Performance**: Optimized data structures for different use cases
- **Maintainability**: Clean, readable code with functional programming
- **Thread Safety**: Proper synchronization for concurrent operations
- **Flexibility**: Easy to extend and modify collection operations