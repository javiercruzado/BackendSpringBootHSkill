package jacc.hyperskill.banking;


import java.math.BigDecimal;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        String databaseName = "db.s3db";
        if (args.length > 1) {
            databaseName = args[1];
        }

        new AccountManager(databaseName);

        Scanner scanner = new Scanner(System.in);

        boolean askForInput = true;
        do {
            System.out.println("1. Create an account");
            System.out.println("2. Log into account");
            System.out.println("0. Exit");
            String input = scanner.nextLine();
            System.out.println();
            switch (input) {
                case "1" -> {
                    var bankAccount = AccountManager.createNewAccount();
                    if (bankAccount != null) {
                        System.out.println("Your card has been created");
                        System.out.printf("Your card number:%n%s%nYour card PIN:%n%s%n%n",
                                bankAccount.getCardNumber(), bankAccount.getPin());
                    } else {
                        System.out.println("Error creating the bank account");
                    }
                }
                case "2" -> {
                    System.out.println("Enter your card number:");
                    String cardNumber = scanner.nextLine();
                    System.out.println("Enter your PIN:");
                    String pin = scanner.nextLine();
                    var bankAccount = AccountManager.getBankAccount(cardNumber, pin);
                    if (bankAccount != null) {
                        System.out.println("You have successfully logged in!");
                        boolean isLogged = true;
                        do {
                            System.out.println("1. Balance");
                            System.out.println("2. Add income");
                            System.out.println("3. Do transfer");
                            System.out.println("4. Close account");
                            System.out.println("5. Log out");
                            System.out.println("0. Exit");
                            String lInput = scanner.nextLine();
                            System.out.println();

                            switch (lInput) {
                                case "1" -> System.out.printf("Balance: %s%n%n", bankAccount.getBalance());
                                case "2" -> {
                                    System.out.println("Enter income:");
                                    String incomeToAdd = scanner.nextLine();
                                    try {
                                        double incomeValue = Double.parseDouble(incomeToAdd);
                                        bankAccount.addIncome(BigDecimal.valueOf(incomeValue));
                                        if (AccountManager.updateBankAccount(bankAccount)) {
                                            System.out.println("Income was added!");
                                        } else {
                                            System.out.println("Something went wrong");
                                        }
                                    } catch (NumberFormatException ex) {
                                        System.out.println("Invalid value");
                                    }
                                }
                                case "3" -> {
                                    System.out.println("Transfer");
                                    System.out.println("Enter card number:");
                                    String cardNumberToTransfer = scanner.nextLine();
                                    if (!BankAccount.BankAccountHelper.isValidLuhn(cardNumberToTransfer)) {
                                        System.out.println("Probably you made a mistake in the card number. Please try again!");
                                        System.out.println();
                                    } else if (!AccountManager.bankAccountExists(cardNumberToTransfer)) {
                                        System.out.println("Such a card does not exist.");
                                        System.out.println();
                                    } else {
                                        System.out.println("Enter how much money you want to transfer:");
                                        String amountToTransfer = scanner.nextLine();
                                        try {
                                            double amountToTransferValue = Double.parseDouble(amountToTransfer);
                                            if (amountToTransferValue > bankAccount.getBalance().doubleValue()) {
                                                System.out.println("Not enough money!");
                                            } else {
                                                boolean transferred = AccountManager.transferMoney(bankAccount.getCardNumber(), cardNumberToTransfer,
                                                        BigDecimal.valueOf(amountToTransferValue));
                                                if (transferred) {
                                                    System.out.println("Success!");
                                                    bankAccount = AccountManager.getBankAccount(cardNumber, pin);
                                                } else {
                                                    System.out.println("Something went wrong!");
                                                }
                                            }
                                        } catch (NumberFormatException ex) {
                                            System.out.println("Invalid value");
                                        }
                                    }
                                }
                                case "4" -> {
                                    if (AccountManager.deleteBankAccount(bankAccount.getCardNumber())) {
                                        System.out.println("The account has been closed!");
                                    } else {
                                        System.out.println("Something went wrong!");
                                    }
                                }
                                case "5" -> isLogged = false;
                                case "0" -> {
                                    isLogged = false;
                                    askForInput = false;
                                }
                            }
                        } while (isLogged);
                    } else {
                        System.out.println("Wrong card number or PIN!");
                    }
                }
                case "0" -> askForInput = false;
            }
        } while (askForInput);

        scanner.close();
    }
}