# Bank Account Management System

## Project Overview

A comprehensive bank account management system built with Java 21, featuring collections, functional programming, file persistence, regex validation, and concurrency.

## Features Implemented

### 1. Collections Migration with Functional Programming
- Replaced arrays with ArrayList and HashMap for efficient data management
- Used `ConcurrentHashMap` and `Collections.synchronizedList` for thread safety
- Implemented functional stream processing for filtering, mapping, and sorting
- Applied Lambda expressions for concise data processing

### 2. File Persistence with Functional Stream Processing
- Implemented file I/O using Java NIO (Paths & Files)
- Used functional streams to map lines to objects during load/save operations
- Automatic loading of data on startup and saving on exit

### 3. Regex Validation
- Account number validation (pattern: `ACC\d{3}`)
- Email validation using regex patterns
- Phone number validation
- Centralized validation logic in `ValidationUtils`

### 4. Thread-Safe Concurrent Transactions
- Used synchronized methods to ensure thread safety
- Implemented thread pools for concurrent transaction simulation
- Applied `AtomicInteger` for generating unique transaction IDs

### 5. Enhanced Console Experience
- Clear menu navigation
- Real-time transaction logging
- User-friendly error messages

## Architecture

### Models
- `Account`: Abstract base class for accounts
- `SavingsAccount` & `CheckingAccount`: Concrete account implementations
- `Customer`: Abstract base class for customers
- `RegularCustomer` & `PremiumCustomer`: Customer implementations
- `Transaction`: Transaction records

### Services
- `AccountManager`: Manages account operations with collections
- `TransactionManager`: Handles transactions with thread safety
- `FilePersistenceService`: Manages file I/O operations

### Utilities
- `ValidationUtils`: Regex-based validation with functional predicates
- `ConcurrencyUtils`: Thread management and simulation utilities

## Key Functional Programming Features Used

1. **Streams API**:
   ```java
   accounts.values().stream()
           .filter(condition)
           .collect(Collectors.toList());
   ```

2. **Lambda Expressions**:
   ```java
   public static final Predicate<String> isValidAccountNumber = 
       accountNumber -> accountNumber != null && accountNumberRegex.matcher(accountNumber).matches();
   ```

3. **Method References**:
   ```java
   transactions.stream()
       .sorted(Comparator.comparing(Transaction::getAmount).reversed())
       .forEach(System.out::println);
   ```

## Concurrency Implementation

- Synchronized methods in AccountManager and TransactionManager
- Thread-safe collections using `Collections.synchronizedList` and `ConcurrentHashMap`
- ExecutorService for managing concurrent transactions
- Atomic operations for generating unique IDs

## File Structure

```
bank-account-management-system/
  src/
    Main.java
    models/
      Account.java
      SavingsAccount.java
      CheckingAccount.java
      Customer.java
      RegularCustomer.java
      PremiumCustomer.java
      Transaction.java
    services/
      AccountManager.java
      TransactionManager.java
      FilePersistenceService.java
    utils/
      ValidationUtils.java
      ConcurrencyUtils.java
  data/
    accounts.txt
    transactions.txt
  docs/
    collections-architecture.md
    README.md
```

## Getting Started

1. Compile the application:
   ```bash
   javac -d bin src/models/*.java src/services/*.java src/utils/*.java src/Main.java
   ```

2. Run the application:
   ```bash
   java -cp bin Main
   ```

## Testing the Application

1. Create accounts with proper validation
2. Perform transactions (deposit, withdrawal, transfer)
3. Test concurrent operations
4. Verify data persistence through save/load functionality
5. Generate account statements

## Code Quality

- Proper use of collections (ArrayList, HashMap, ConcurrentHashMap)
- Thread safety with synchronized methods
- Functional programming with streams and lambdas
- Comprehensive input validation with regex
- Clean, modular architecture