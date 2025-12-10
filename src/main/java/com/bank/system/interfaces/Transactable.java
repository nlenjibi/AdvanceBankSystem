package com.bank.system.interfaces;

import com.bank.system.enums.TransactionType;

public interface Transactable {
    // type: "DEPOSIT" or "WITHDRAWAL"
    boolean  processTransaction(double amount, TransactionType type);

}
