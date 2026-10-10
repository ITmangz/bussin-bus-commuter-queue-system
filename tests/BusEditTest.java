import java.sql.*;
import java.util.*;
import qpal.dao.BusDao;
import qpal.util.DbConnection;

public class BusEditTest {
    public static void main(String[] args) throws Exception {
        String schema = "qpal_integration_test_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection c = DbConnection.getConnection();
                Statement s = c.createStatement()) {
            s.executeUpdate("CREATE DATABASE " + schema);
            try {
                for (String table : List.of("buses", "trips", "bookings"))
                    s.executeUpdate("CREATE TABLE " + schema + "." + table + " LIKE qpal." + table);
                System.setProperty("qpal.db.url", "jdbc:mysql://localhost:3306/" + schema);
                s.executeUpdate(
                        "INSERT INTO "
                                + schema
                                + ".buses(bus_id,bus_number,seat_capacity,available_seats,bus_status)"
                                + " VALUES(1,'EDIT-TEST',40,40,'Available')");
                BusDao dao = new BusDao();
                var bus = dao.getBus(1);
                bus.setSeatCapacity(28);
                bus.setAvailableSeats(28);
                if (!dao.updateBus(bus)) throw new AssertionError("Cannot edit bus without trips");
                if (!dao.updateBus(bus)) throw new AssertionError("Unchanged save fails");
                s.executeUpdate(
                        "INSERT INTO "
                                + schema
                                + ".trips(trip_id,bus_id,route_id,departure_date,departure_time,available_seats,status)"
                                + " VALUES(1,1,1,CURRENT_DATE,'12:00:00',28,'Departed'),(2,1,1,CURRENT_DATE+INTERVAL"
                                + " 1 DAY,'12:00:00',28,'Scheduled')");
                s.executeUpdate(
                        "INSERT INTO "
                                + schema
                                + ".bookings(booking_id,booking_reference,trip_id,total_amount,status)"
                                + " VALUES(1,'HISTORY',1,50,'Confirmed')");
                bus.setSeatCapacity(32);
                bus.setAvailableSeats(32);
                if (!dao.updateBusDetails(bus))
                    throw new AssertionError("Historical booking blocks edit");
                try (ResultSet r =
                        s.executeQuery(
                                "SELECT available_seats FROM "
                                        + schema
                                        + ".trips WHERE trip_id=2")) {
                    r.next();
                    if (r.getInt(1) != 32) throw new AssertionError("Unbooked trip capacity stale");
                }
                s.executeUpdate(
                        "INSERT INTO "
                                + schema
                                + ".bookings(booking_id,booking_reference,trip_id,total_amount,status)"
                                + " VALUES(2,'ACTIVE',2,50,'Pending')");
                bus.setSeatCapacity(36);
                bus.setAvailableSeats(36);
                try {
                    dao.updateBusDetails(bus);
                    throw new AssertionError("Booked active capacity changed");
                } catch (SQLException expected) {
                }
                if (dao.getBus(1).getSeatCapacity() != 32)
                    throw new AssertionError("Failed edit changed bus");
                bus.setSeatCapacity(32);
                bus.setAvailableSeats(32);
                bus.setBusNumber("RENAMED");
                bus.setBusStatus("Maintenance");
                if (!dao.updateBusDetails(bus))
                    throw new AssertionError("Details with unchanged capacity blocked");
                s.executeUpdate(
                        "UPDATE " + schema + ".trips SET status='Cancelled' WHERE trip_id=2");
                bus.setSeatCapacity(36);
                bus.setAvailableSeats(36);
                if (!dao.updateBusDetails(bus)) throw new AssertionError("Closed trips block edit");
                System.out.println(
                        "PASS: trip-free edits, unchanged saves, historical bookings, active"
                            + " booking protection, rollback, name/status edits and trip capacity"
                            + " sync");
            } finally {
                System.clearProperty("qpal.db.url");
                s.executeUpdate("DROP DATABASE " + schema);
            }
        }
    }
}
