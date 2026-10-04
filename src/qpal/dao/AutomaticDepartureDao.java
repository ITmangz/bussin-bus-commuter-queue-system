package qpal.dao;

import java.sql.*;
import java.util.ArrayList;
import static qpal.dao.BookingDao.*;

/** Atomic and repeatable scheduled departure, independent of staff station selection. */
public final class AutomaticDepartureDao {
    private AutomaticDepartureDao() {}

    public static void departDue(Connection c) throws SQLException {
        if (!c.getAutoCommit()) throw new SQLException("Departure requires its own transaction.");
        BoardingGateDao.ensure(c);
        QueueDao.ensureStations(c);
        QueueDao.ensureBoardingTable(c);
        var trips = new ArrayList<Integer>();
        try (PreparedStatement p = c.prepareStatement("SELECT trip_id FROM trips WHERE status IN ('Scheduled','Boarding') AND TIMESTAMP(departure_date,departure_time)<=NOW() ORDER BY trip_id");
                ResultSet r = p.executeQuery()) {
            while (r.next()) trips.add(r.getInt(1));
        }
        for (int trip : trips) {
            c.setAutoCommit(false);
            try {
                // Match the gate-before-trip lock order used by manual gate actions.
                try (PreparedStatement p = c.prepareStatement("SELECT gate FROM boarding_gates ORDER BY gate FOR UPDATE");
                        ResultSet r = p.executeQuery()) { while (r.next()) { } }
                try (PreparedStatement p = statement(c,"SELECT trip_id FROM trips WHERE trip_id=? AND status IN ('Scheduled','Boarding') AND TIMESTAMP(departure_date,departure_time)<=NOW() FOR UPDATE",trip);
                        ResultSet r = p.executeQuery()) {
                    if (!r.next()) { c.rollback(); continue; }
                }
                update(c,"UPDATE bookings b SET b.status='No-show' WHERE b.trip_id=? AND b.status NOT IN ('Cancelled','Expired','No-show') "
                        + "AND NOT EXISTS(SELECT 1 FROM queue_entries q JOIN queue_boarding qb ON qb.queue_entry_id=q.queue_entry_id WHERE q.booking_id=b.booking_id)",trip);
                update(c,"UPDATE queue_entries q JOIN bookings b ON b.booking_id=q.booking_id SET q.status='No-show' WHERE b.trip_id=? AND b.status='No-show'",trip);
                update(c,"DELETE s FROM queue_stations s JOIN boarding_gates g ON g.gate=s.station WHERE s.kind='Boarding' AND g.trip_id=?",trip);
                update(c,"UPDATE boarding_gates SET trip_id=NULL WHERE trip_id=?",trip);
                update(c,"UPDATE trips SET status='Departed' WHERE trip_id=?",trip);
                c.commit();
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
}
