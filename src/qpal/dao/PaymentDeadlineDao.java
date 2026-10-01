package qpal.dao;

import java.sql.*;
import java.util.ArrayList;
import static qpal.dao.BookingDao.*;

/** Payment and expiry serialize on the trip row before touching a booking. */
public final class PaymentDeadlineDao {
    private PaymentDeadlineDao() {}

    public static void requireOpen(Connection c, int queue) throws SQLException {
        int trip;
        try (PreparedStatement p = statement(c, "SELECT b.trip_id FROM bookings b JOIN queue_entries q ON q.booking_id=b.booking_id WHERE q.queue_entry_id=?", queue);
                ResultSet r = p.executeQuery()) {
            if (!r.next()) throw new SQLException("Queue no longer exists.");
            trip = r.getInt(1);
        }
        try (PreparedStatement p = statement(c, "SELECT status,TIMESTAMP(departure_date,departure_time)>DATE_ADD(NOW(),INTERVAL 30 MINUTE) FROM trips WHERE trip_id=? FOR UPDATE", trip);
                ResultSet r = p.executeQuery()) {
            if (!r.next() || !r.getBoolean(2) || !java.util.Set.of("Scheduled","Boarding").contains(r.getString(1)))
                throw new SQLException("Payment closes 30 minutes before departure. This unpaid reservation has expired; refresh the queue.");
        }
    }

    public static void expire(Connection c) throws SQLException {
        if (!c.getAutoCommit()) throw new SQLException("Expiry requires its own transaction.");
        var trips = new ArrayList<Integer>();
        try (PreparedStatement p = c.prepareStatement("SELECT trip_id FROM trips WHERE status IN ('Scheduled','Boarding') AND TIMESTAMP(departure_date,departure_time)<=DATE_ADD(NOW(),INTERVAL 30 MINUTE)"); ResultSet r = p.executeQuery()) {
            while(r.next()) trips.add(r.getInt(1));
        }
        for (int trip : trips) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement p = statement(c,"SELECT trip_id FROM trips WHERE trip_id=? AND status IN ('Scheduled','Boarding') AND TIMESTAMP(departure_date,departure_time)<=DATE_ADD(NOW(),INTERVAL 30 MINUTE) FOR UPDATE",trip); ResultSet r=p.executeQuery()) {
                    if (!r.next()) { c.rollback(); continue; }
                }
                var bookings = new ArrayList<Integer>();
                try (PreparedStatement p=statement(c,"SELECT booking_id FROM bookings WHERE trip_id=? AND status='Pending' FOR UPDATE",trip); ResultSet r=p.executeQuery()) {
                    while(r.next()) bookings.add(r.getInt(1));
                }
                for (int booking : bookings) {
                    boolean paid;
                    try (PreparedStatement p=statement(c,"SELECT status FROM payments WHERE booking_id=? ORDER BY payment_id DESC LIMIT 1 FOR UPDATE",booking); ResultSet r=p.executeQuery()) {
                        paid=r.next() && "Paid".equals(r.getString(1));
                    }
                    if (paid) continue;
                    int released=update(c,"UPDATE seat_reservations s JOIN booking_passengers bp ON bp.booking_passenger_id=s.booking_passenger_id SET s.status='Expired' WHERE bp.booking_id=? AND s.status='Active'",booking);
                    update(c,"UPDATE trips SET available_seats=available_seats+? WHERE trip_id=?",released,trip);
                    update(c,"UPDATE payments SET status='Cancelled' WHERE booking_id=? AND status='Pending'",booking);
                    update(c,"UPDATE bookings SET status='No-show' WHERE booking_id=?",booking);
                    update(c,"UPDATE queue_entries SET status='No-show' WHERE booking_id=?",booking);
                }
                c.commit();
            } catch(SQLException | RuntimeException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }
}
