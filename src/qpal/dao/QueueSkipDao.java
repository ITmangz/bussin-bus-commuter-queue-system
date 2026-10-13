package qpal.dao;

import java.sql.*;
import static qpal.dao.BookingDao.*;

/** Payment skip counts survive recalls and application restarts. */
final class QueueSkipDao {
    static void ensureTable(Connection c) throws SQLException {
        update(c, "CREATE TABLE IF NOT EXISTS payment_queue_skips (queue_entry_id INT PRIMARY KEY,"
                + " skip_count INT NOT NULL DEFAULT 0) ENGINE=InnoDB");
    }

    // Match booking/payment lock order: trip before queue and seat changes.
    static void lockTrip(Connection c, int queue) throws SQLException {
        int trip;
        try (PreparedStatement p = statement(c,
                "SELECT b.trip_id FROM bookings b JOIN queue_entries q ON q.booking_id=b.booking_id"
                        + " WHERE q.queue_entry_id=?", queue);
                ResultSet r = p.executeQuery()) {
            if (!r.next()) throw new SQLException("Queue no longer exists.");
            trip = r.getInt(1);
        }
        try (PreparedStatement p = statement(c,
                "SELECT trip_id FROM trips WHERE trip_id=? FOR UPDATE", trip);
                ResultSet r = p.executeQuery()) {
            if (!r.next()) throw new SQLException("Trip no longer exists.");
        }
    }

    // Caller holds the trip and serving queue locks in the same transaction.
    static void skip(Connection c, int queue, int booking) throws SQLException {
        update(c, "INSERT INTO payment_queue_skips(queue_entry_id,skip_count) VALUES(?,1)"
                + " ON DUPLICATE KEY UPDATE skip_count=skip_count+1", queue);
        int count;
        try (PreparedStatement p = statement(c,
                "SELECT skip_count FROM payment_queue_skips WHERE queue_entry_id=?", queue);
                ResultSet r = p.executeQuery()) {
            r.next();
            count = r.getInt(1);
        }
        if (count < 3) {
            update(c, "UPDATE queue_entries SET status='Skipped' WHERE queue_entry_id=?", queue);
            return;
        }
        int freed = update(c,
                "UPDATE seat_reservations s JOIN booking_passengers bp ON"
                        + " bp.booking_passenger_id=s.booking_passenger_id SET s.status='Expired'"
                        + " WHERE bp.booking_id=? AND s.status='Active'", booking);
        update(c, "UPDATE trips SET available_seats=available_seats+? WHERE trip_id="
                + "(SELECT trip_id FROM bookings WHERE booking_id=?)", freed, booking);
        update(c, "UPDATE bookings SET status='No-show' WHERE booking_id=?", booking);
        update(c, "UPDATE queue_entries SET status='No-show' WHERE queue_entry_id=?", queue);
        update(c, "UPDATE payments SET status='Cancelled' WHERE booking_id=? AND status='Pending'", booking);
        update(c, "DELETE FROM queue_stations WHERE kind='Payment' AND queue_entry_id=?", queue);
    }
}
