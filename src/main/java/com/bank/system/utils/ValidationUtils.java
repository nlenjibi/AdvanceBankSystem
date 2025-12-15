package com.bank.system.utils;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ValidationUtils {

    // Regex patterns
    private static final String ACCOUNT_NUMBER_PATTERN = "^ACC\\d{3}$";
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
    //    private static final String PHONE_PATTERN = "^\\+?[1-9]\\d{1,14}$"; // International phone format
    private static final String NAME_PATTERN = "^[A-Za-zÀ-ÖØ-öø-ÿ\\s'-]+$";
    private static final String ADDRESS_PATTERN = "^[A-Za-z0-9\\s,.\\-]+$";
    //private static final String PHONE_PATTERN ="^((\\+233|0)[235]\\d{2}(-?\\d{3}){2}|\\+?[1-9]\\d{0,3}-?(-?\\d{2,4}){1,3})$";

    private static final String PHONE_PATTERN ="^(\\+?[1-9]\\d{6,14}|(\\+233|0)[235]\\d{8})$";


    private static final Pattern accountNumberRegex = Pattern.compile(ACCOUNT_NUMBER_PATTERN);
    private static final Pattern emailRegex = Pattern.compile(EMAIL_PATTERN);
    private static final Pattern phoneRegex = Pattern.compile(PHONE_PATTERN);
    private static final Pattern nameRegex = Pattern.compile(NAME_PATTERN);
    private static final Pattern addressRegex = Pattern.compile(ADDRESS_PATTERN);


    // Predicates for validation
    public static final Predicate<String> isValidAccountNumber =
            accountNumber -> accountNumber != null && !accountNumber.isEmpty() && accountNumberRegex.matcher(accountNumber).matches();

    public static final Predicate<String> isValidEmail =
            email -> email != null && emailRegex.matcher(email.trim()).matches();

    public static final Predicate<String> isValidPhone =
                    phone -> phone != null && !phone.trim().isEmpty() && phoneRegex.matcher(normalizePhone(phone).trim()).matches();
    public static final Predicate<String> isValidName =
            name -> name !=null && !name.trim().isEmpty() && nameRegex.matcher(name.trim()).matches();
    public static final Predicate<String> isValidAddress =
            address -> address != null && !address.trim().isEmpty() && addressRegex.matcher(address.trim()).matches();
    public static final Predicate<Integer> isValidAge =
            age -> age != null && age >= 0 && age <= 150;

    public static final Predicate<Double> isValidAmount = amount -> amount != null && amount>0;


    public static boolean validateAccountNumber(String accountNumber) {
        return isValidAccountNumber.test(accountNumber);
    }


    public static boolean validateEmail(String email) {
        return isValidEmail.test(email);
    }


    public static boolean validatePhone(String phone) {
        return isValidPhone.test(phone);
    }
    public static boolean validateAddress(String address) {
        return isValidAddress.test(address);
    }
    public static boolean validateName(String name) {
        return isValidName.test(name);
    }
    public static boolean validateAmount(double amount) {
        return isValidAmount.test(amount);
    }
    public static boolean validateContactNumber(String contactNumber) {
        return isValidPhone.test(contactNumber);
    }
    public static boolean validateAge(int age) {
        return isValidAge.test(age);
    }
    private static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        // Keep digits and leading +
        return phone.replaceAll("[^\\d+]", "");
    }









}