package com.bank.system.models;

import com.bank.system.interfaces.CustomerService;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class Customer implements CustomerService {
    private final String customerId;
    private final String name;
    private final int age;
    private final String contact;
    private final String address;
    private static final AtomicInteger CUSTOMER_COUNTER = new AtomicInteger(0);

    protected Customer(String name, int age, String contact, String address) {
        this.name = name;
        this.age = age;
        this.contact = contact;
        this.address = address;
        this.customerId = generateCustomerId();
    }

    private static String generateCustomerId() {
        return String.format("CUS%03d", CUSTOMER_COUNTER.incrementAndGet());
    }

    // Abstract methods to be implemented by subclasses

    // Getters and setters

    public String getName() {
        return name;
    }

    public String getCustomerId() {
        return customerId;
    }

    public int getAge() {
        return age;
    }

    public String getContact() {
        return contact;
    }

    public String getAddress() {
        return address;
    }

    public String getDisplayLabel() {
        return name + " (" + customerId + ")";
    }

}