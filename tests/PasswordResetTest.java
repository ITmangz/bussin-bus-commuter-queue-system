import java.sql.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import qpal.util.*;

public class PasswordResetTest {
    interface Action {
        void run() throws Exception;
    }

    static void rejects(Action action) throws Exception {
        try {
            action.run();
            throw new AssertionError("Expected rejection");
        } catch (IllegalArgumentException expected) {
        }
    }

    static String hash(String text) throws Exception {
        return HexFormat.of()
                .formatHex(
                        MessageDigest.getInstance("SHA-256")
                                .digest(text.getBytes(StandardCharsets.UTF_8)));
    }

    static void seed(Connection c) throws Exception {
        try (PreparedStatement p =
                c.prepareStatement(
                        "REPLACE INTO"
                            + " password_reset_codes(account_id,email,salt,code_hash,expires_at,sent_at)"
                            + " VALUES(1,'test@example.com','salt',?,DATE_ADD(NOW(),INTERVAL 5"
                            + " MINUTE),NOW())")) {
            p.setString(1, hash("salt123456"));
            p.executeUpdate();
        }
    }

    public static void main(String[] args) throws Exception {
        for (String email :
                new String[] {"person@gmail.com", "person@outlook.com", "student@school.edu.ph"})
            if (!EmailSender.validEmail(email)) throw new AssertionError("Valid provider rejected");
        for (String email :
                new String[] {
                    "enzo@",
                    "abc",
                    "person @gmail.com",
                    "Person <person@gmail.com>",
                    "a..b@gmail.com"
                })
            if (EmailSender.validEmail(email)) throw new AssertionError("Invalid email accepted");
        String schema =
                "qpal_integration_test_" + java.util.UUID.randomUUID().toString().replace("-", "");
        try (Connection setup = DbConnection.getConnection();
                Statement s = setup.createStatement()) {
            s.executeUpdate("CREATE DATABASE " + schema);
            System.setProperty("qpal.db.url", "jdbc:mysql://localhost:3306/" + schema);
            try (Connection c = DbConnection.getConnection();
                    Statement sql = c.createStatement()) {
                sql.executeUpdate(
                        "CREATE TABLE accounts(id INT PRIMARY KEY,email VARCHAR(100),password"
                            + " VARCHAR(100),status VARCHAR(20))");
                sql.executeUpdate(
                        "INSERT INTO accounts VALUES(1,'test@example.com','old','Active')");
                // An unknown account must fail before any email is sent.
                try {
                    PasswordResetService.send("missing@example.com");
                    throw new AssertionError("Unknown email accepted");
                } catch (IllegalArgumentException ex) {
                    if (!"Email address not found.".equals(ex.getMessage())) throw ex;
                }
                rejects(() -> PasswordResetService.reset("test@example.com", "fake", "new"));
                seed(c);
                rejects(() -> PasswordResetService.send("test@example.com"));
                for (int i = 0; i < 5; i++)
                    rejects(() -> PasswordResetService.verify("test@example.com", "999999"));
                rejects(() -> PasswordResetService.verify("test@example.com", "123456"));
                seed(c);
                sql.executeUpdate(
                        "UPDATE password_reset_codes SET expires_at=DATE_SUB(NOW(),INTERVAL 1"
                            + " SECOND)");
                rejects(() -> PasswordResetService.verify("test@example.com", "123456"));
                seed(c);
                String token = PasswordResetService.verify("test@example.com", "123456");
                rejects(() -> PasswordResetService.verify("test@example.com", "123456"));
                rejects(() -> PasswordResetService.reset("another@example.com", token, "new"));
                PasswordResetService.reset("test@example.com", token, "new");
                rejects(() -> PasswordResetService.reset("test@example.com", token, "again"));
                try (ResultSet r = sql.executeQuery("SELECT password FROM accounts WHERE id=1")) {
                    r.next();
                    if (!r.getString(1).equals("new"))
                        throw new AssertionError("Password not saved");
                }
                seed(c);
                final String expiredToken =
                        PasswordResetService.verify("test@example.com", "123456");
                sql.executeUpdate(
                        "UPDATE password_reset_codes SET token_expires=DATE_SUB(NOW(),INTERVAL 1"
                            + " SECOND)");
                rejects(() -> PasswordResetService.reset("test@example.com", expiredToken, "no"));
                seed(c);
                sql.executeUpdate("UPDATE accounts SET status='Inactive'");
                rejects(() -> PasswordResetService.verify("test@example.com", "123456"));
                System.out.println(
                        "PASS: email formats, cooldown, expiry, five attempts, account binding,"
                            + " single-use code/token, password update, inactive accounts. No email"
                            + " sent.");
            } finally {
                System.clearProperty("qpal.db.url");
                s.executeUpdate("DROP DATABASE " + schema);
            }
        }
    }
}
