package qpal.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import qpal.util.DbConnection;
import static qpal.dao.BookingDao.*;

/** Explicit, transactional deletion used only after the UI's cascading-delete warning. */
public class DeleteDao {
    public enum Target { BUS, ROUTE, TRIP }

    public boolean delete(Target target, int id) throws SQLException {
        String table = target == Target.BUS ? "buses" : target == Target.ROUTE ? "routes" : "trips";
        String key = target == Target.BUS ? "bus_id" : target == Target.ROUTE ? "route_id" : "trip_id";
        try (Connection c = DbConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement p = statement(c, "SELECT " + key + " FROM " + table + " WHERE " + key + "=? FOR UPDATE", id);
                        ResultSet r = p.executeQuery()) {
                    if (!r.next()) { c.rollback(); return false; }
                }
                List<Integer> trips = new ArrayList<>();
                try (PreparedStatement p = statement(c, "SELECT trip_id FROM trips WHERE " + key + "=? ORDER BY trip_id FOR UPDATE", id);
                        ResultSet r = p.executeQuery()) {
                    while (r.next()) trips.add(r.getInt(1));
                }
                for (int trip : trips) {
                    // Lock bookings before deleting dependents; foreign keys remain enabled.
                    try (PreparedStatement p = statement(c, "SELECT booking_id FROM bookings WHERE trip_id=? FOR UPDATE", trip);
                            ResultSet r = p.executeQuery()) { while (r.next()) { r.getInt(1); } }
                    update(c, "DELETE s FROM seat_reservations s JOIN booking_passengers bp ON bp.booking_passenger_id=s.booking_passenger_id "
                            + "JOIN bookings bk ON bk.booking_id=bp.booking_id WHERE bk.trip_id=?", trip);
                    update(c, "DELETE FROM seat_reservations WHERE trip_id=?", trip);
                    update(c, "DELETE q FROM queue_entries q JOIN bookings bk ON bk.booking_id=q.booking_id WHERE bk.trip_id=?", trip);
                    update(c, "DELETE p FROM payments p LEFT JOIN bookings bk ON bk.booking_id=p.booking_id WHERE p.trip_id=? OR bk.trip_id=?", trip, trip);
                    update(c, "DELETE bp FROM booking_passengers bp JOIN bookings bk ON bk.booking_id=bp.booking_id WHERE bk.trip_id=?", trip);
                    update(c, "DELETE FROM bookings WHERE trip_id=?", trip);
                    update(c, "DELETE FROM trips WHERE trip_id=?", trip);
                }
                if (target != Target.TRIP) update(c, "DELETE FROM " + table + " WHERE " + key + "=?", id);
                c.commit();
                return true;
            } catch (SQLException | RuntimeException ex) { c.rollback(); throw ex; }
        }
    }
}
