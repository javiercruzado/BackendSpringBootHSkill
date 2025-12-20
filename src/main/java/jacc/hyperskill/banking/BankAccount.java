package jacc.hyperskill.banking;

import java.math.BigDecimal;

public class BankAccount {

    final private String cardNumber;
    final private String pin;
    private BigDecimal balance;

    public BankAccount(String cardNumber, String pin, BigDecimal balance) {
        this.cardNumber = cardNumber;
        this.pin = pin;
        this.balance = balance;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getPin() {
        return pin;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void addIncome(BigDecimal income) {
        balance = balance.add(income);
    }

    static class BankAccountHelper {
        public static int calculateLuhnChecksum(String numberWithoutChecksum) {
            int sum = 0;
            boolean doubleDigit = true;

            for (int i = numberWithoutChecksum.length() - 1; i >= 0; i--) {
                int digit = numberWithoutChecksum.charAt(i) - '0';

                if (doubleDigit) {
                    digit *= 2;
                    if (digit > 9) digit -= 9;
                }

                sum += digit;
                doubleDigit = !doubleDigit;
            }

            return (10 - (sum % 10)) % 10;
        }

        public static boolean isValidLuhn(String cardNumber) {
            int sum = 0;
            boolean doubleDigit = false;

            for (int i = cardNumber.length() - 1; i >= 0; i--) {
                int digit = cardNumber.charAt(i) - '0';

                if (doubleDigit) {
                    digit *= 2;
                    if (digit > 9) digit -= 9;
                }

                sum += digit;
                doubleDigit = !doubleDigit;
            }

            return sum % 10 == 0;
        }
    }
}
