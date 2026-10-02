package qpal.dao;

import java.sql.*;
import java.util.*;
import qpal.util.DbConnection;
import static qpal.dao.BookingDao.*;

public class QueueBookingDao {
    public record Person(int id, String name, String type) {}

    public List<Person> passengers(int booking) throws SQLException {
        try (Connection c = DbConnection.getConnection(); PreparedStatement p = statement(c,
                "SELECT booking_passenger_id,passenger_name,passenger_type FROM booking_passengers WHERE booking_id=? ORDER BY booking_passenger_id", booking);
                ResultSet r = p.executeQuery()) {
            List<Person> result = new ArrayList<>();
            while (r.next()) result.add(new Person(r.getInt(1),r.getString(2),r.getString(3)));
            return result;
        }
    }

    public void edit(int booking, List<Person> original, List<Person> people) throws SQLException {
        if (people.isEmpty() || people.size()!=original.size()) throw new SQLException("Reopen the passenger details.");
        for (Person person : people) if (person.name().trim().isEmpty() || person.name().trim().length()>100
                || !Set.of("Regular","Student","Senior","PWD").contains(person.type()))
            throw new SQLException("Enter passenger names of 1–100 characters and valid passenger types.");
        try (Connection c = DbConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement p = statement(c,"SELECT status FROM bookings WHERE booking_id=? FOR UPDATE",booking);
                        ResultSet r = p.executeQuery()) {
                    if (!r.next() || Set.of("Cancelled","Completed").contains(r.getString(1)))
                        throw new SQLException("Closed bookings cannot be edited.");
                }
                for (int i=0;i<people.size();i++) {
                    Person old = original.get(i), person = people.get(i);
                    if (old.id()!=person.id()) throw new SQLException("Passenger list changed. Reopen the dialog.");
                    try (PreparedStatement p = statement(c,"SELECT passenger_name,passenger_type FROM booking_passengers "
                            + "WHERE booking_passenger_id=? AND booking_id=? FOR UPDATE",old.id(),booking); ResultSet r=p.executeQuery()) {
                        if (!r.next() || !old.name().equals(r.getString(1)) || !old.type().equals(r.getString(2)))
                            throw new SQLException("Passenger details changed. Reopen the dialog.");
                    }
                    update(c,"UPDATE booking_passengers SET passenger_name=?,passenger_type=? WHERE booking_passenger_id=?",
                            person.name().trim(),person.type(),person.id());
                }
                update(c,"UPDATE payments SET commuter_name=? WHERE booking_id=?",people.get(0).name().trim(),booking);
                c.commit();
                ActivityLogDao.recordActivity("Queue Management", "Update", "Updated passenger details for booking #" + booking + ".");
            } catch (SQLException | RuntimeException ex) { c.rollback(); throw ex; }
        }
    }

    public void cancel(int booking) throws SQLException {
        try (Connection c = DbConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int trip;
                try (PreparedStatement p=statement(c,"SELECT trip_id FROM bookings WHERE booking_id=?",booking); ResultSet r=p.executeQuery()) {
                    if (!r.next()) throw new SQLException("Booking no longer exists.");
                    trip=r.getInt(1);
                }
                try (PreparedStatement p=statement(c,"SELECT trip_id FROM trips WHERE trip_id=? FOR UPDATE",trip); ResultSet r=p.executeQuery()) {
                    if (!r.next()) throw new SQLException("Trip no longer exists.");
                }
                update(c,"INSERT INTO queue_daily_counters(queue_date,last_queue_number) VALUES(CURRENT_DATE,0) "
                        + "ON DUPLICATE KEY UPDATE last_queue_number=last_queue_number");
                try (PreparedStatement p=statement(c,"SELECT status FROM bookings WHERE booking_id=? FOR UPDATE",booking); ResultSet r=p.executeQuery()) {
                    if (!r.next() || Set.of("Cancelled","Completed").contains(r.getString(1)))
                        throw new SQLException("This booking is already closed.");
                }
                int freed = update(c,"DELETE s FROM seat_reservations s JOIN booking_passengers bp "
                        + "ON bp.booking_passenger_id=s.booking_passenger_id WHERE bp.booking_id=? AND s.status='Active'",booking);
                update(c,"UPDATE trips SET available_seats=available_seats+? WHERE trip_id=?",freed,trip);
                update(c,"UPDATE queue_entries SET status='Cancelled' WHERE booking_id=?",booking);
                update(c,"UPDATE bookings SET status='Cancelled' WHERE booking_id=?",booking);
                // Preserve collected payments; cancelling a booking is not a refund.
                update(c,"UPDATE payments SET status='Cancelled' WHERE booking_id=? AND status='Pending'",booking);
                c.commit();
                ActivityLogDao.recordActivity("Queue Management", "Delete", "Cancelled booking #" + booking + " and released its seats.");
            } catch (SQLException | RuntimeException ex) { c.rollback(); throw ex; }
        }
    }
}
