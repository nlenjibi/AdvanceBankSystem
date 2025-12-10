package com.bank.system.utils;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ValidationUtils {

    // Regex patterns
    private static final String ACCOUNT_NUMBER_PATTERN = "^ACC\\d{3}$";
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
    //    private static final String PHONE_PATTERN = "^\\+?[1-9]\\d{1,14}$"; // International phone format
    private static final String PHONE_PATTERN ="^(\\+?[1-9]\\d{1,14}|0[235][0-9]{8}|(\\+?233)[235][0-9]{8})$";
    private static final String NAME_PATTERN = "^[A-Za-zÀ-ÖØ-öø-ÿ\\s'-]+$";
    private static final String ADDRESS_PATTERN = "^[A-Za-z0-9\\s,.\\-]+$";

    private static final Pattern accountNumberRegex = Pattern.compile(ACCOUNT_NUMBER_PATTERN);
    private static final Pattern emailRegex = Pattern.compile(EMAIL_PATTERN);
    private static final Pattern phoneRegex = Pattern.compile(PHONE_PATTERN);
    private static final Pattern nameRegex = Pattern.compile(NAME_PATTERN);
    private static final Pattern addressRegex = Pattern.compile(ADDRESS_PATTERN);


    // Predicates for validation
    public static final Predicate<String> isValidAccountNumber =
            accountNumber -> accountNumber != null && !accountNumber.isEmpty() && accountNumberRegex.matcher(accountNumber).matches();

    public static final Predicate<String> isValidEmail =
            email -> email != null && emailRegex.matcher(email).matches();

    public static final Predicate<String> isValidPhone =
            phone -> phone != null && phone.trim().isEmpty() && phoneRegex.matcher(phone).matches();
    public static final Predicate<String> isValidName =
            name -> name !=null && !name.trim().isEmpty() && nameRegex.matcher(name).matches();
    public static final Predicate<String> isValidAddress =
            address -> address != null && !address.trim().isEmpty() && addressRegex.matcher(address).matches();



}