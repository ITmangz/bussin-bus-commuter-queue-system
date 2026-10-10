import java.sql.*;
import qpal.util.DbConnection;
import qpal.dao.PaymentDeadlineDao;

public class PaymentDeadlineTest {
    static void check(Connection c, String sql, int expected) throws Exception {
        try (var p = c.prepareStatement(sql);
                var r = p.executeQuery()) {
            r.next();
            if (r.getInt(1) != expected) throw new AssertionError(sql);
        }
    }

    public static void main(String[] args) throws Exception {
        try (Connection c = DbConnection.getConnection();
                Statement s = c.createStatement()) {
            s.executeUpdate(
                    "CREATE TEMPORARY TABLE trips(trip_id INT PRIMARY KEY,status"
                        + " VARCHAR(20),departure_date DATE,departure_time TIME,available_seats"
                        + " INT) ENGINE=InnoDB");
            s.executeUpdate(
                    "CREATE TEMPORARY TABLE bookings(booking_id INT PRIMARY KEY,trip_id INT,status"
                        + " VARCHAR(20)) ENGINE=InnoDB");
            s.executeUpdate(
                    "CREATE TEMPORARY TABLE payments(payment_id INT PRIMARY KEY,booking_id"
                        + " INT,status VARCHAR(20)) ENGINE=InnoDB");
            s.executeUpdate(
                    "CREATE TEMPORARY TABLE queue_entries(queue_entry_id INT PRIMARY KEY,booking_id"
                        + " INT,status VARCHAR(20)) ENGINE=InnoDB");
            s.executeUpdate(
                    "CREATE TEMPORARY TABLE booking_passengers(booking_passenger_id INT PRIMARY"
                        + " KEY,booking_id INT) ENGINE=InnoDB");
            s.executeUpdate(
                    "CREATE TEMPORARY TABLE seat_reservations(booking_passenger_id INT PRIMARY"
                        + " KEY,status VARCHAR(20)) ENGINE=InnoDB");
            s.executeUpdate(
                    "INSERT INTO trips"
                        + " VALUES(1,'Scheduled',DATE(NOW()),TIME(NOW()),7),(2,'Boarding',DATE(DATE_ADD(NOW(),INTERVAL"
                        + " 9 MINUTE)),TIME(DATE_ADD(NOW(),INTERVAL 9 MINUTE)),9)");
            s.executeUpdate(
                    "INSERT INTO bookings VALUES(1,1,'Pending'),(2,1,'Confirmed'),(3,2,'Pending')");
            s.executeUpdate(
                    "INSERT INTO payments VALUES(1,1,'Pending'),(2,2,'Paid'),(3,3,'Pending')");
            s.executeUpdate(
                    "INSERT INTO queue_entries"
                        + " VALUES(1,1,'Skipped'),(2,2,'Completed'),(3,3,'Serving')");
            s.executeUpdate("INSERT INTO booking_passengers VALUES(1,1),(2,1),(3,2),(4,3)");
            s.executeUpdate(
                    "INSERT INTO seat_reservations"
                        + " VALUES(1,'Active'),(2,'Active'),(3,'Active'),(4,'Active')");
            c.setAutoCommit(false);
            try {
                PaymentDeadlineDao.requireOpen(c, 1);
                throw new AssertionError("Cutoff accepted");
            } catch (SQLException expected) {
            }
            PaymentDeadlineDao.requireOpen(c, 3);
            c.rollback();
            c.setAutoCommit(true);
            PaymentDeadlineDao.expire(c);
            PaymentDeadlineDao.expire(c);
            check(c, "SELECT available_seats FROM trips WHERE trip_id=1", 9);
            check(c, "SELECT COUNT(*) FROM bookings WHERE status='No-show'", 1);
            check(c, "SELECT COUNT(*) FROM seat_reservations WHERE status='Active'", 2);
            check(c, "SELECT COUNT(*) FROM payments WHERE status='Paid'", 1);
            check(c, "SELECT COUNT(*) FROM queue_entries WHERE status='No-show'", 1);
            check(c, "SELECT COUNT(*) FROM trips WHERE status='Departed'", 0);
            System.out.println(
                    "PASS: cutoff rejects payment; future payment accepted; skipped unpaid seats"
                        + " released once; paid seats preserved; expiry leaves departure to the"
                        + " departure service.");
        }
    }
}
