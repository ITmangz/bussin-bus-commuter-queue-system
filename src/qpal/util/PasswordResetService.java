package qpal.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.util.HexFormat;

/** Password-reset challenges shared across app windows and restarts. */
public final class PasswordResetService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private static String hash(String text) throws Exception {
        return HexFormat.of()
                .formatHex(
                        MessageDigest.getInstance("SHA-256")
                                .digest(text.getBytes(StandardCharsets.UTF_8)));
    }

    private static void ensure(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS password_reset_codes (account_id INT PRIMARY KEY,"
                        + " email VARCHAR(100) NOT NULL, salt VARCHAR(64) NOT NULL, code_hash"
                        + " VARCHAR(64) NOT NULL, expires_at DATETIME NOT NULL, sent_at DATETIME"
                        + " NOT NULL, attempts INT NOT NULL DEFAULT 0, token_hash VARCHAR(64) NULL,"
                        + " token_expires DATETIME NULL) ENGINE=InnoDB");
        }
    }

    public static void send(String email) throws Exception {
        if (!EmailSender.validEmail(email))
            throw new IllegalArgumentException("Enter a valid email address.");
        try (Connection c = DbConnection.getConnection()) {
            ensure(c);
            c.setAutoCommit(false);
            try {
                int account;
                try (PreparedStatement p =
                        c.prepareStatement(
                                "SELECT id FROM accounts WHERE email=? AND status='Active' FOR"
                                    + " UPDATE")) {
                    p.setString(1, email);
                    try (ResultSet r = p.executeQuery()) {
                        // Same UI response for unknown or inactive addresses.
                        if (!r.next()) {
                            c.commit();
                            return;
                        }
                        account = r.getInt(1);
                        if (r.next())
                            throw new IllegalArgumentException(
                                    "This email is used by multiple accounts. Please contact the"
                                        + " administrator.");
                    }
                }
                try (PreparedStatement p =
                        c.prepareStatement(
                                "SELECT sent_at>DATE_SUB(NOW(),INTERVAL 60 SECOND) FROM"
                                    + " password_reset_codes WHERE account_id=? FOR UPDATE")) {
                    p.setInt(1, account);
                    try (ResultSet r = p.executeQuery()) {
                        if (r.next() && r.getBoolean(1))
                            throw new IllegalArgumentException(
                                    "Please wait 60 seconds before requesting another code.");
                    }
                }
                String code = String.format(java.util.Locale.ROOT, "%06d", RANDOM.nextInt(1000000));
                byte[] bytes = new byte[32];
                RANDOM.nextBytes(bytes);
                String salt = HexFormat.of().formatHex(bytes);
                try (PreparedStatement p =
                        c.prepareStatement(
                                "REPLACE INTO"
                                    + " password_reset_codes(account_id,email,salt,code_hash,expires_at,sent_at)"
                                    + " VALUES(?,?,?,?,DATE_ADD(NOW(),INTERVAL 5 MINUTE),NOW())")) {
                    p.setInt(1, account);
                    p.setString(2, email);
                    p.setString(3, salt);
                    p.setString(4, hash(salt + code));
                    p.executeUpdate();
                }
                EmailSender.sendResetCode(email, code);
                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    public static String verify(String email, String code) throws Exception {
        if (!code.matches("[0-9]{6}"))
            throw new IllegalArgumentException("Enter the six-digit code.");
        try (Connection c = DbConnection.getConnection()) {
            ensure(c);
            c.setAutoCommit(false);
            try {
                int account;
                String salt;
                String stored;
                try (PreparedStatement p =
                        c.prepareStatement(
                                "SELECT r.account_id,r.salt,r.code_hash FROM password_reset_codes r"
                                    + " JOIN accounts a ON a.id=r.account_id WHERE r.email=? AND"
                                    + " a.email=r.email AND a.status='Active' AND"
                                    + " r.expires_at>NOW() AND r.attempts<5 AND r.token_hash IS"
                                    + " NULL FOR UPDATE")) {
                    p.setString(1, email);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next())
                            throw new IllegalArgumentException(
                                    "Code expired or unavailable. Request a new code.");
                        account = r.getInt(1);
                        salt = r.getString(2);
                        stored = r.getString(3);
                    }
                }
                if (!MessageDigest.isEqual(
                        hash(salt + code).getBytes(StandardCharsets.UTF_8),
                        stored.getBytes(StandardCharsets.UTF_8))) {
                    try (PreparedStatement p =
                            c.prepareStatement(
                                    "UPDATE password_reset_codes SET attempts=attempts+1 WHERE"
                                        + " account_id=?")) {
                        p.setInt(1, account);
                        p.executeUpdate();
                    }
                    c.commit();
                    throw new IllegalArgumentException(
                            "Incorrect code. After five incorrect attempts, request a new code.");
                }
                byte[] bytes = new byte[32];
                RANDOM.nextBytes(bytes);
                String token = HexFormat.of().formatHex(bytes);
                try (PreparedStatement p =
                        c.prepareStatement(
                                "UPDATE password_reset_codes SET"
                                    + " token_hash=?,token_expires=DATE_ADD(NOW(),INTERVAL 5"
                                    + " MINUTE) WHERE account_id=?")) {
                    p.setString(1, hash(token));
                    p.setInt(2, account);
                    p.executeUpdate();
                }
                c.commit();
                return token;
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    public static void reset(String email, String token, String password) throws Exception {
        if (password.isBlank() || password.length() > 100)
            throw new IllegalArgumentException("Enter a password of 1–100 characters.");
        if (token == null) throw new IllegalArgumentException("Verify your email code first.");
        try (Connection c = DbConnection.getConnection()) {
            ensure(c);
            c.setAutoCommit(false);
            try {
                int account;
                try (PreparedStatement p =
                        c.prepareStatement(
                                "SELECT a.id FROM accounts a JOIN password_reset_codes r ON"
                                    + " a.id=r.account_id WHERE r.email=? AND a.email=r.email AND"
                                    + " a.status='Active' AND r.token_hash=? AND"
                                    + " r.token_expires>NOW() FOR UPDATE")) {
                    p.setString(1, email);
                    p.setString(2, hash(token));
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next())
                            throw new IllegalArgumentException(
                                    "Reset authorization expired or used. Request a new code.");
                        account = r.getInt(1);
                    }
                }
                // Preserve compatibility with the existing account password storage and login.
                try (PreparedStatement p =
                        c.prepareStatement("UPDATE accounts SET password=? WHERE id=?")) {
                    p.setString(1, password);
                    p.setInt(2, account);
                    p.executeUpdate();
                }
                try (PreparedStatement p =
                        c.prepareStatement("DELETE FROM password_reset_codes WHERE account_id=?")) {
                    p.setInt(1, account);
                    p.executeUpdate();
                }
                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    public static String errorMessage(Exception ex) {
        if (ex instanceof IllegalArgumentException || ex instanceof IllegalStateException)
            return ex.getMessage();
        if (ex instanceof jakarta.mail.MessagingException)
            return "Unable to send email. Check the sender settings and internet connection, then"
                       + " retry.";
        return "Unable to complete the request. Check the database connection and retry.";
    }
}
