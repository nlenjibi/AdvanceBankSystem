package com.bank.system.utils;

import java.util.Scanner;
import java.util.function.Predicate;

public class ConsoleUtil {
    private static final Scanner scanner = new Scanner(System.in);
    private static final int CONSOLE_WIDTH = 63;
    // Constants for consistent formatting
    private static final String SEPARATOR = "=".repeat(CONSOLE_WIDTH);
    private static final String SUB_SEPARATOR = "-".repeat(CONSOLE_WIDTH);



    public static void printHeader(String title) {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println(centerText(title));
        System.out.println(SEPARATOR);
    }

    public static void printSeparator() {
        print(SUB_SEPARATOR);
    }

    public static String subSeparator(int str) {
        return "-".repeat(str);

    }
    public static String separator(int str) {
        return "=".repeat(str);

    }

    private static String centerText(String text) {
        if (text.length() >= CONSOLE_WIDTH) {
            return text;
        }
        int padding = (CONSOLE_WIDTH - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }



        // Read a full line
    public static String readString(String prompt, Predicate<String> validator, String errorMessage) {
        while (true) {
            pr(prompt);
            String input = scanner.nextLine().trim();

            if (validator.test(input)) {
                return input;
            }
            print(errorMessage);
        }
    }

    // Read an integer safely

    public static int getValidIntInput(String prompt, int min, int max) {
        while (true) {
            pr(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                print("Input cannot be empty. Please try again.");
                continue;
            }
            try {

                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                printf("Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                print("Invalid input. Please enter a valid number: ");
            }
        }
    }
    public static double getValidDoubleInput(String prompt, Predicate<Double> validator, String errorMessage) {
        while (true) {
            pr(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                print("Input cannot be empty. Please try again.");
                continue;
            }
            try {
                // Remove $ or commas if user types e.g. $1,500
                input = input.replaceAll("[$,]", "");
                double value = Double.parseDouble(input);
                if (validator.test(value)) {

                    return value;
                }
                print(errorMessage);
            } catch (NumberFormatException e) {
                print("Invalid input. Please enter a valid number: $");
            }
        }
    }
    public static String underline(String text, char lineChar) {
        int length = text.length();
        StringBuilder sb = new StringBuilder();

        // Add the text
        sb.append(text).append("\n");

        // Draw the line with same number of characters
        for (int i = 0; i <= length; i++) {
            sb.append(lineChar);
        }

        return sb.toString();
    }



    // Pause the console
    public static void pressEnterToContinue() {
        IO.println("\nPress Enter to continue…");
        scanner.nextLine();
    }
    public static void print(Object text) {
        IO.println(text);
    }

    public static void printf(String format, Object ... args) {
        System.out.printf(format, args);
    }
    // a custom print method to displace text to the console
    public static void pr(Object text) {
        IO.print(text);
    }
    public void clearScreen(){
        print("\033[H\033[2J");
        System.out.flush();
    }
    public static void waitForEnter(String message) {
        pr(message);
        scanner.nextLine();
    }
    public static void formatCurrency(String label, double amount) {
        printf("%s: $%.2f%n", label, amount);
    }
    public static boolean readConfirmation() {
        while (true) {
            pr("Confirm transaction?" + " (Y/N): ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("Y") || input.equals("YES")) {
                return true;
            } else if (input.equals("N") || input.equals("NO")) {
                return false;
            }
            print("Please enter Y for Yes or N for No.");
        }
    }

    public static void printKeyValue(String key, String value) {
        printf("%-20s: %s%n", key, value);
    }
}