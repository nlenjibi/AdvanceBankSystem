package com.bank.system.interfaces;

import com.bank.system.enums.TransactionType;
import com.bank.system.models.Account;

public interface Transactable {
    // type: "DEPOSIT" or "WITHDRAWAL"
    boolean  processTransaction(double amount, TransactionType type);

}
