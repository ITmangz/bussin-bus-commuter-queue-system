package qpal.dao;

import java.sql.*;
import java.util.*;
import qpal.util.DbConnection;
import static qpal.dao.BookingDao.*;

public class BoardingGateDao {
    public record GateTrip(int id, String description) {
        @Override
        public String toString() {
            return description;
        }
    }

    public static void ensure(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS boarding_gates (gate INT PRIMARY KEY,trip_id INT"
                        + " NULL UNIQUE) ENGINE=InnoDB");
            s.executeUpdate("INSERT IGNORE INTO boarding_gates(gate) VALUES(1),(2)");
            s.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS boarding_skips (queue_entry_id INT PRIMARY"
                        + " KEY,skipped_at DATETIME NOT NULL) ENGINE=InnoDB");
        }
    }

    public List<GateTrip> waitingTrips() throws SQLException {
        try (Connection c = DbConnection.getConnection()) {
            ensure(c);
            var result = new ArrayList<GateTrip>();
            try (PreparedStatement p =
                            c.prepareStatement(
                                    "SELECT"
                                        + " t.trip_id,b.bus_number,r.origin,r.destination,t.departure_date,t.departure_time"
                                        + " FROM trips t JOIN buses b ON b.bus_id=t.bus_id JOIN"
                                        + " routes r ON r.route_id=t.route_id WHERE t.status IN"
                                        + " ('Scheduled','Boarding') AND b.bus_status='Available'"
                                        + " AND NOT EXISTS(SELECT 1 FROM boarding_gates g WHERE"
                                        + " g.trip_id=t.trip_id) ORDER BY"
                                        + " t.departure_date,t.departure_time,t.trip_id");
                    ResultSet r = p.executeQuery()) {
                while (r.next())
                    result.add(
                            new GateTrip(
                                    r.getInt(1),
                                    r.getString(2)
                                            + " • "
                                            + r.getString(3)
                                            + " → "
                                            + r.getString(4)
                                            + " • "
                                            + r.getString(5)
                                            + " "
                                            + r.getString(6)));
            }
            return result;
        }
    }

    public void assign(int gate, int trip) throws SQLException {
        if (gate < 1 || gate > 2) throw new SQLException("Select Gate 1 or Gate 2 first.");
        try (Connection c = DbConnection.getConnection()) {
            ensure(c);
            c.setAutoCommit(false);
            try {
                EmployeeStationDao.requireStation(c, "Boarding", gate);
                try (PreparedStatement p =
                                c.prepareStatement(
                                        "SELECT gate,trip_id FROM boarding_gates ORDER BY gate FOR"
                                            + " UPDATE");
                        ResultSet r = p.executeQuery()) {
                    while (r.next())
                        if ((r.getInt(1) == gate && r.getObject(2) != null)
                                || (r.getObject(2) != null && r.getInt(2) == trip))
                            throw new SQLException(
                                    "Gate or trip is already assigned. Release the gate first.");
                }
                try (PreparedStatement p =
                                statement(
                                        c,
                                        "SELECT t.status,b.bus_status FROM trips t JOIN buses b ON"
                                            + " b.bus_id=t.bus_id WHERE t.trip_id=? FOR UPDATE",
                                        trip);
                        ResultSet r = p.executeQuery()) {
                    if (!r.next()
                            || !Set.of("Scheduled", "Boarding").contains(r.getString(1))
                            || !"Available".equals(r.getString(2)))
                        throw new SQLException("Trip or bus is no longer available for boarding.");
                }
                update(c, "UPDATE boarding_gates SET trip_id=? WHERE gate=?", trip, gate);
                update(c, "UPDATE trips SET status='Boarding' WHERE trip_id=?", trip);
                c.commit();
                ActivityLogDao.recordActivity(
                        "Queue Management",
                        "Update",
                        "Assigned trip #" + trip + " to gate " + gate + ".");
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    public static int assignedTrip(Connection c, int gate) throws SQLException {
        try (PreparedStatement p =
                        statement(
                                c,
                                "SELECT trip_id FROM boarding_gates WHERE gate=? FOR UPDATE",
                                gate);
                ResultSet r = p.executeQuery()) {
            if (!r.next() || r.getObject(1) == null)
                throw new SQLException("Assign a trip to this gate first.");
            return r.getInt(1);
        }
    }

    public void close(int gate, boolean depart, boolean noShows) throws SQLException {
        try (Connection c = DbConnection.getConnection()) {
            ensure(c);
            new QueueDao().stations("Boarding");
            new QueueDao().boarding();
            c.setAutoCommit(false);
            try {
                EmployeeStationDao.requireStation(c, "Boarding", gate);
                int trip = assignedTrip(c, gate);
                try (PreparedStatement p =
                                statement(
                                        c,
                                        "SELECT trip_id FROM trips WHERE trip_id=? FOR UPDATE",
                                        trip);
                        ResultSet r = p.executeQuery()) {
                    if (!r.next()) throw new SQLException("Trip no longer exists.");
                }
                if (depart)
                    try (PreparedStatement p =
                                    statement(
                                            c,
                                            "SELECT booking_id FROM bookings WHERE trip_id=? AND"
                                                + " status='Pending' LIMIT 1",
                                            trip);
                            ResultSet r = p.executeQuery()) {
                        if (r.next())
                            throw new SQLException(
                                    "This trip still has unpaid reservations. Resolve payments or"
                                        + " wait for the payment cutoff before departing.");
                    }
                String unresolved =
                        "SELECT q.booking_id FROM queue_entries q JOIN bookings b ON"
                            + " b.booking_id=q.booking_id WHERE b.trip_id=? AND b.status NOT IN"
                            + " ('Cancelled','Expired','No-show') AND EXISTS(SELECT 1 FROM payments"
                            + " p WHERE p.booking_id=b.booking_id AND p.status='Paid') AND NOT"
                            + " EXISTS(SELECT 1 FROM queue_boarding qb WHERE"
                            + " qb.queue_entry_id=q.queue_entry_id)";
                var missing = new ArrayList<Integer>();
                try (PreparedStatement p = statement(c, unresolved, trip);
                        ResultSet r = p.executeQuery()) {
                    while (r.next()) missing.add(r.getInt(1));
                }
                if (depart && !missing.isEmpty() && !noShows)
                    throw new SQLException(
                            "Paid passengers are still waiting. Complete boarding or confirm"
                                + " remaining passengers as No-show.");
                if (noShows)
                    for (int booking : missing)
                        update(
                                c,
                                "UPDATE bookings SET status='No-show' WHERE booking_id=?",
                                booking);
                update(
                        c,
                        "UPDATE trips SET status=? WHERE trip_id=?",
                        depart ? "Departed" : "Scheduled",
                        trip);
                update(c, "UPDATE boarding_gates SET trip_id=NULL WHERE gate=?", gate);
                update(c, "DELETE FROM queue_stations WHERE kind='Boarding' AND station=?", gate);
                c.commit();
                ActivityLogDao.recordActivity(
                        "Queue Management",
                        "Update",
                        (depart ? "Departed trip and closed" : "Closed") + " gate " + gate + ".");
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        }
    }
}
