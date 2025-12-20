package jacc.hyperskill.banking;

import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

public class AccountManager {

    private final String databaseName;
    private static String dataBaseUrl;

    AccountManager(String dbName) {
        this.databaseName = dbName;
        setup();
    }

    public void setup() {

        dataBaseUrl = String.format("jdbc:sqlite:%s", databaseName);

        try (var conn = DriverManager.getConnection(dataBaseUrl);
             var stmt = conn.createStatement()) {
            System.out.println("Connection to SQLite has been established.");

            var sql = "CREATE TABLE IF NOT EXISTS card ("
                    + "	id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "	number TEXT NOT NULL,"
                    + "	pin TEXT, "
                    + " balance INTEGER DEFAULT 0"
                    + ");";
            stmt.execute(sql);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static BankAccount createNewAccount() {
        var cardNumber = generateCardNumber();
        BankAccount bankAccount = new BankAccount(cardNumber, generatePin(), new BigDecimal(0));

        var sql = "INSERT into card(number, pin, balance) VALUES(?,?,?)";
        try (var conn = DriverManager.getConnection(dataBaseUrl);
             var pStmt = conn.prepareStatement(sql)) {

            pStmt.setString(1, bankAccount.getCardNumber());
            pStmt.setString(2, bankAccount.getPin());
            pStmt.setBigDecimal(3, bankAccount.getBalance());
            var result = pStmt.executeUpdate();
            if (result == 0) {
                return null;
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return bankAccount;
    }

    public static BankAccount getBankAccount(String cardNumber, String pin) {

        BankAccount bankAccount = null;

        var sql = "SELECT number, pin, balance FROM card WHERE number = ?";

        try (var conn = DriverManager.getConnection(dataBaseUrl);
             var pStmt = conn.prepareStatement(sql)) {

            pStmt.setString(1, cardNumber);
            var rs = pStmt.executeQuery();
            if (rs.next()) {
                var dbPin = rs.getString("pin");
                if (pin.equals(rs.getString("pin"))) {
                    var dbBalance = rs.getBigDecimal("balance");
                    bankAccount = new BankAccount(cardNumber, dbPin, dbBalance);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return bankAccount;
    }

    public static boolean updateBankAccount(BankAccount bankAccount) {

        boolean updated = false;
        var sql = "update card set balance=? where number=?";
        try (var conn = DriverManager.getConnection(dataBaseUrl);
             var pStmt = conn.prepareStatement(sql)) {

            pStmt.setBigDecimal(1, bankAccount.getBalance());
            pStmt.setString(2, bankAccount.getCardNumber());

            var result = pStmt.executeUpdate();
            if (result > 0) {
                updated = true;
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return updated;
    }

    public static boolean deleteBankAccount(String cardNumber) {

        boolean deleted = false;
        var sql = "DELETE from card WHERE number=?";

        try (var conn = DriverManager.getConnection(dataBaseUrl);
             var pStmt = conn.prepareStatement(sql)) {

            pStmt.setString(1, cardNumber);
            var result = pStmt.executeUpdate();
            if (result > 0) {
                deleted = true;
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return deleted;
    }

    public static boolean bankAccountExists(String cardNumber) {

        boolean bankAccountExists = false;

        var sql = "SELECT 1 FROM card WHERE number = ?";

        try (var conn = DriverManager.getConnection(dataBaseUrl);
             var pStmt = conn.prepareStatement(sql)) {

            pStmt.setString(1, cardNumber);
            var rs = pStmt.executeQuery();
            if (rs.next()) {
                bankAccountExists = true;
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return bankAccountExists;
    }
    public static boolean transferMoney(String sourceAccount, String targetAccount, BigDecimal amountToTransfer) {

        boolean transferred = false;
        var sqlUpdateSource = "UPDATE card set balance = balance - ? WHERE number = ?";
        var sqlUpdateTarget = "UPDATE card set balance = balance + ? WHERE number = ?";

        try (var conn = DriverManager.getConnection(dataBaseUrl);
             var pStmt = conn.prepareStatement(sqlUpdateSource);
             var pStmtTgt = conn.prepareStatement(sqlUpdateTarget)) {

            conn.setAutoCommit(false);

            pStmt.setBigDecimal(1, amountToTransfer);
            pStmt.setString(2, sourceAccount);
            var result = pStmt.executeUpdate();
            if (result > 0) {
                pStmtTgt.setBigDecimal(1, amountToTransfer);
                pStmtTgt.setString(2, targetAccount);
                result = pStmtTgt.executeUpdate();
                conn.commit();
                transferred = result > 0;
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return transferred;
    }

    private static String generatePin() {
        int num = ThreadLocalRandom.current().nextInt(9999);
        return String.format("%04d", num);
    }

    private static final String IIN = "400000";

    public static String generateCardNumber() {
        // Generate a 9-digit account number (to make total = 16 digits)
        int accountNumber = ThreadLocalRandom.current().nextInt(100_000_000, 1_000_000_000);

        // Choose any checksum digit (0–9)
        int checksum = BankAccount.BankAccountHelper.calculateLuhnChecksum(IIN + accountNumber);

        return IIN + accountNumber + checksum;
    }

}