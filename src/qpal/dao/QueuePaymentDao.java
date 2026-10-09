package qpal.dao;

import java.math.BigDecimal;
import java.sql.*;
import qpal.util.DbConnection;
import static qpal.dao.BookingDao.*;

/** Durable counter payment and document progress, independent of the open dialog. */
public class QueuePaymentDao {
    public record Progress(BigDecimal received, boolean receiptPrinted, boolean ticketsPrinted) {}

    public static void ensureTable(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS cashless_verifications (payment_id INT PRIMARY KEY, "
                    + "payment_method VARCHAR(20) NOT NULL, transaction_reference VARCHAR(64) NOT NULL, "
                    + "verified_by INT NULL, verified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "UNIQUE KEY cashless_reference(payment_method,transaction_reference)) ENGINE=InnoDB");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS queue_payment_progress (queue_entry_id INT PRIMARY KEY, "
                    + "received DECIMAL(12,2) NULL, receipt_printed BOOLEAN NOT NULL DEFAULT FALSE, "
                    + "tickets_printed BOOLEAN NOT NULL DEFAULT FALSE, "
                    + "FOREIGN KEY(queue_entry_id) REFERENCES queue_entries(queue_entry_id) ON DELETE CASCADE) ENGINE=InnoDB");
        }
    }

    public Progress progress(int queue) throws SQLException {
        try (Connection c = DbConnection.getConnection()) {
            ensureTable(c);
            try (PreparedStatement p = statement(c,"SELECT received,receipt_printed,tickets_printed FROM queue_payment_progress WHERE queue_entry_id=?",queue);
                    ResultSet r = p.executeQuery()) {
                return r.next() ? new Progress(r.getBigDecimal(1),r.getBoolean(2),r.getBoolean(3)) : new Progress(null,false,false);
            }
        }
    }

    public static BigDecimal validateReceived(String value, BigDecimal total) {
        BigDecimal amount;
        try {
            String input=value.trim();
            if (!input.matches("(?:[0-9]+|[0-9]{1,2},[0-9]{3})(\\.[0-9]{1,2})?"))
                throw new IllegalArgumentException();
            amount = new BigDecimal(input.replace(",", "")).setScale(2, java.math.RoundingMode.UNNECESSARY);
        }
        catch (RuntimeException ex) { throw new IllegalArgumentException("Enter a valid amount with at most two decimal places."); }
        if (amount.signum() < 0 || amount.compareTo(new BigDecimal("10000")) > 0)
            throw new IllegalArgumentException("Amount received must not exceed PHP 10,000.");
        if (amount.compareTo(total) < 0) throw new IllegalArgumentException("Amount received must cover the total fare.");
        return amount;
    }

    public void requireBoardingPayment(int queue) throws SQLException {
        try (Connection c = DbConnection.getConnection()) {
            ensureTable(c);
            try (PreparedStatement p = statement(c,"SELECT q.status,pp.receipt_printed,"
                    + "(SELECT status FROM payments WHERE booking_id=q.booking_id ORDER BY payment_id DESC LIMIT 1) "
                    + "FROM queue_entries q LEFT JOIN queue_payment_progress pp ON pp.queue_entry_id=q.queue_entry_id "
                    + "WHERE q.queue_entry_id=?",queue); ResultSet r = p.executeQuery()) {
                if (!r.next() || "Cancelled".equals(r.getString(1)) || !r.getBoolean(2) || !"Paid".equals(r.getString(3)))
                    throw new SQLException("Finish Payment and collect the payment receipt before printing boarding tickets.");
            }
        }
    }

    public void pay(int queue, int station, BigDecimal received) throws SQLException {
        pay(queue,station,received,null,false);
    }

    public void pay(int queue, int station, BigDecimal received, String reference, boolean verified) throws SQLException {
        try (Connection c = DbConnection.getConnection()) {
            ensureTable(c);
            c.setAutoCommit(false);
            try {
                EmployeeStationDao.requireStation(c,"Payment",station);
                PaymentDeadlineDao.requireOpen(c, queue);
                int booking;
                try (PreparedStatement p = statement(c,"SELECT q.booking_id FROM queue_entries q JOIN queue_stations s "
                        + "ON s.queue_entry_id=q.queue_entry_id AND s.kind='Payment' WHERE q.queue_entry_id=? "
                        + "AND q.status='Serving' AND q.queue_date=CURRENT_DATE AND s.station=? FOR UPDATE",queue,station);
                        ResultSet r = p.executeQuery()) {
                    if (!r.next()) throw new SQLException("Call this queue at your counter before taking payment.");
                    booking = r.getInt(1);
                }
                int payment;
                try (PreparedStatement p = statement(c,"SELECT payment_id,amount,status,payment_method FROM payments WHERE booking_id=? ORDER BY payment_id DESC LIMIT 1 FOR UPDATE",booking);
                        ResultSet r = p.executeQuery()) {
                    if (!r.next() || !"Pending".equals(r.getString(3))) throw new SQLException("Payment is no longer pending. Reopen Payment.");
                    payment = r.getInt(1);
                    validateReceived(received.toPlainString(),r.getBigDecimal(2));
                    if (!"Cash".equals(r.getString(4)) && received.compareTo(r.getBigDecimal(2)) != 0)
                        throw new SQLException("Cashless payment must equal the exact fare.");
                    if (!"Cash".equals(r.getString(4))) {
                        if (!verified || reference == null || !reference.trim().matches("[A-Za-z0-9-]{1,64}"))
                            throw new SQLException("Verify the merchant record or terminal approval and enter its transaction reference.");
                        var account = ActivityLogDao.getCurrentAccount();
                        if (account == null) throw new SQLException("Sign in before verifying payment.");
                        update(c,"INSERT INTO cashless_verifications(payment_id,payment_method,transaction_reference,verified_by) VALUES(?,?,?,?)",
                                payment,r.getString(4),reference.trim().toUpperCase(java.util.Locale.ROOT),account.getID());
                    }
                }
                PaymentDeadlineDao.requireOpen(c, queue);
                update(c,"UPDATE payments SET status='Paid',paid_at=NOW() WHERE payment_id=?",payment);
                update(c,"UPDATE bookings SET status='Confirmed' WHERE booking_id=?",booking);
                update(c,"INSERT INTO queue_payment_progress(queue_entry_id,received) VALUES(?,?)",queue,received);
                EmployeeStationDao.recordWork(c,"Payment","Payment",station,queue);
                c.commit();
                ActivityLogDao.recordActivity("Queue Management", "Update", "Recorded payment for queue #" + queue + " at counter " + station + ".");
            } catch (SQLException | RuntimeException ex) { c.rollback(); throw ex; }
        }
    }

    public void printed(int queue, boolean tickets) throws SQLException {
        try (Connection c = DbConnection.getConnection()) {
            ensureTable(c);
            c.setAutoCommit(false);
            try {
                EmployeeStationDao.requireQueue(c,"Payment",queue);
                try (PreparedStatement p = statement(c,"SELECT q.status,(SELECT status FROM payments WHERE booking_id=q.booking_id ORDER BY payment_id DESC LIMIT 1) "
                        + "FROM queue_entries q WHERE queue_entry_id=? FOR UPDATE",queue); ResultSet r=p.executeQuery()) {
                    if (!r.next() || "Cancelled".equals(r.getString(1)) || !"Paid".equals(r.getString(2)))
                        throw new SQLException("Only paid, active bookings can be printed.");
                }
                update(c,"INSERT IGNORE INTO queue_payment_progress(queue_entry_id) VALUES(?)",queue);
                if (tickets) {
                    if (update(c,"UPDATE queue_payment_progress SET tickets_printed=TRUE WHERE queue_entry_id=? AND receipt_printed=TRUE",queue)==0)
                        throw new SQLException("Print and collect the payment receipt first.");
                } else update(c,"UPDATE queue_payment_progress SET receipt_printed=TRUE WHERE queue_entry_id=?",queue);
                int ticketCount = 0;
                if (tickets) {
                    try (PreparedStatement p = statement(c,"SELECT COUNT(*) FROM booking_passengers bp JOIN queue_entries q ON q.booking_id=bp.booking_id WHERE q.queue_entry_id=?",queue);
                            ResultSet r = p.executeQuery()) {
                        r.next(); ticketCount = r.getInt(1);
                    }
                }
                c.commit();
                ActivityLogDao.recordActivity("Queue Management", "Print", "Printed " + (tickets ? ticketCount + " boarding ticket(s) for " + ticketCount + " passenger(s)" : "receipt") + " for queue #" + queue + ".");
            } catch (SQLException | RuntimeException ex) { c.rollback(); throw ex; }
        }
    }

    public static void requirePrinted(Connection c, int queue) throws SQLException {
        try (PreparedStatement p = statement(c,"SELECT tickets_printed FROM queue_payment_progress WHERE queue_entry_id=?",queue); ResultSet r=p.executeQuery()) {
            if (!r.next() || !r.getBoolean(1)) throw new SQLException("Open Payment and print the receipt and all boarding tickets before completing this queue.");
        }
    }
}
