package com.bank.system.models;

public class PremiumCustomer extends Customer {
    private static final double INTEREST_RATE = 0.035; // 3.5% annual interest
    private static final double MINIMUM_BALANCE = 10000.0;

    public PremiumCustomer(String name, int age, String contact, String address) {
        super(name, age, contact, address);
    }


    @Override
    public String getCustomerType() {
        return "Premium";
    }

    @Override
    public double getInterestRate() {
        return INTEREST_RATE;
    }

    public double getMinimumBalance() {
        return MINIMUM_BALANCE;
    }


}