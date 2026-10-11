package qpal.dao;

import java.sql.*;
import java.util.UUID;
import qpal.util.DbConnection;

public class PaymentMethodMigrationTest {
    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        String schema = "qpal_wallet_test_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection c = DbConnection.getConnection(); Statement setup = c.createStatement()) {
            setup.executeUpdate("CREATE DATABASE " + schema);
            c.setCatalog(schema);
            try (Statement s = c.createStatement()) {
                s.executeUpdate("CREATE TABLE payments (id INT PRIMARY KEY, payment_method ENUM('Cash','GCash','Card','E-Wallet','Legacy Wallet') NOT NULL DEFAULT 'E-Wallet' COMMENT 'Keep history')");
                s.executeUpdate("INSERT INTO payments VALUES (1,'E-Wallet'),(2,'Legacy Wallet'),(3,'GCash')");
                QueuePaymentDao.ensureWalletMethods(c);
                QueuePaymentDao.ensureWalletMethods(c);
                try (ResultSet r = s.executeQuery("SELECT payment_method FROM payments ORDER BY id")) {
                    for (String expected : new String[] {"E-Wallet", "Legacy Wallet", "GCash"}) {
                        check(r.next() && expected.equals(r.getString(1)), "Historical value preserved: " + expected);
                    }
                    check(!r.next(), "No rows added during migration");
                }
                try (ResultSet r = s.executeQuery("SHOW FULL COLUMNS FROM payments LIKE 'payment_method'")) {
                    check(r.next() && "NO".equals(r.getString("Null")), "Nullability preserved");
                    check("E-Wallet".equals(r.getString("Default")), "Default preserved");
                    check("Keep history".equals(r.getString("Comment")), "Comment preserved");
                }
                s.executeUpdate("INSERT INTO payments VALUES (4,'Maya'),(5,'MariBank'),(6,'PayPal')");
                s.executeUpdate("ALTER TABLE payments MODIFY payment_method VARCHAR(40)");
                s.executeUpdate("INSERT INTO payments VALUES (7,'Another legacy value')");
                QueuePaymentDao.ensureWalletMethods(c);
                try (ResultSet r = s.executeQuery("SELECT payment_method FROM payments WHERE id=7")) {
                    check(r.next() && "Another legacy value".equals(r.getString(1)), "Text columns remain unrestricted");
                }
                System.out.println("PASS: historical enum values, row preservation, repeat migration, defaults, new wallets, text columns");
            } finally {
                c.setCatalog("qpal");
                setup.executeUpdate("DROP DATABASE " + schema);
            }
        }
    }
}
