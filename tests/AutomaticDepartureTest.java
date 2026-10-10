import java.sql.*;
import java.util.*;
import qpal.dao.*;
import qpal.model.BookingData.*;
import qpal.util.*;

public class AutomaticDepartureTest {
    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        String schema = "qpal_departure_test_" + UUID.randomUUID().toString().replace("-", "");
        String originalUrl = System.getProperty("qpal.db.url");
        try (Connection setup = DbConnection.getConnection();
                Statement s = setup.createStatement()) {
            s.executeUpdate("CREATE DATABASE " + schema);
            try {
                for (String table :
                        List.of(
                                "buses",
                                "routes",
                                "trips",
                                "bookings",
                                "booking_passengers",
                                "seat_reservations",
                                "payments",
                                "queue_entries",
                                "queue_daily_counters"))
                    s.executeUpdate("CREATE TABLE " + schema + "." + table + " LIKE qpal." + table);
                System.setProperty("qpal.db.url", "jdbc:mysql://localhost:3306/" + schema);
                try (Connection c = DbConnection.getConnection();
                        Statement sql = c.createStatement()) {
                    sql.executeUpdate(
                            "INSERT INTO"
                                + " buses(bus_id,bus_number,seat_capacity,available_seats,bus_status)"
                                + " VALUES(1,'TEST',20,20,'Available')");
                    sql.executeUpdate(
                            "INSERT INTO routes(route_id,origin,destination,fare,status)"
                                + " VALUES(1,'A','B',50,'Active')");
                    sql.executeUpdate(
                            "INSERT INTO"
                                + " trips(trip_id,bus_id,route_id,departure_date,departure_time,available_seats,status)"
                                + " VALUES(1,1,1,CURRENT_DATE+INTERVAL 1"
                                + " DAY,'12:00:00',20,'Boarding'),(2,1,1,CURRENT_DATE-INTERVAL 1"
                                + " DAY,'12:00:00',20,'Boarding'),(3,1,1,CURRENT_DATE-INTERVAL 1"
                                + " DAY,'13:00:00',20,'Scheduled'),(4,1,1,CURRENT_DATE-INTERVAL 1"
                                + " DAY,'14:00:00',20,'Cancelled')");
                    BookingDao booking = new BookingDao();
                    TripOption trip =
                            booking.availableTrips().stream()
                                    .filter(t -> t.id() == 1)
                                    .findFirst()
                                    .orElseThrow();
                    booking.book(
                            "BOARDING-TEST",
                            trip,
                            List.of(new Passenger("Passenger", "Regular", 1)),
                            "Cash");
                    QueueDao queue = new QueueDao();
                    check(queue.boarding().isEmpty(), "Unpaid passengers excluded");
                    queue.act(queue.today().get(0).id(), "Mark as Paid");
                    sql.executeUpdate("UPDATE queue_entries SET status='Completed'");
                    sql.executeUpdate("UPDATE bookings SET status='Completed'");
                    check(queue.boarding().size() == 1, "Paid passenger appears in boarding queue");
                    sql.executeUpdate(
                            "UPDATE queue_entries SET queue_date=CURRENT_DATE-INTERVAL 1 DAY");
                    check(
                            queue.boarding().size() == 1,
                            "Earlier bookings remain in boarding queue");
                    int boardingId = queue.boarding().get(0).id();
                    new BoardingGateDao().assign(1, 1);
                    queue.callBoarding(boardingId, 1, false);
                    try {
                        queue.completeBoarding(boardingId);
                        throw new AssertionError("Unprinted boarding accepted");
                    } catch (SQLException expected) {
                    }
                    new qpal.dao.QueuePaymentDao().printed(boardingId, false);
                    new qpal.dao.QueuePaymentDao().printed(boardingId, true);
                    queue.completeBoarding(boardingId);
                    check(
                            queue.boarding().isEmpty(),
                            "Completed boarding is removed from the list");
                    try {
                        queue.completeBoarding(boardingId);
                        throw new AssertionError("Duplicate boarding accepted");
                    } catch (SQLException expected) {
                    }
                    booking.book(
                            "BOARDING-SECOND",
                            trip,
                            List.of(new Passenger("Second", "Regular", 2)),
                            "Cash");
                    queue.act(queue.today().get(0).id(), "Mark as Paid");
                    sql.executeUpdate("UPDATE queue_entries SET status='Completed'");
                    sql.executeUpdate("UPDATE bookings SET status='Completed'");
                    check(queue.boarding().size() == 1, "Other boarding queues remain actionable");
                    int missingId = queue.boarding().get(0).id();
                    queue.callBoarding(missingId, 1, false);
                    DepartureService.reconcile(c);
                    try (ResultSet r =
                            sql.executeQuery(
                                    "SELECT trip_id,status,available_seats FROM trips ORDER BY"
                                        + " trip_id")) {
                        String[] expected = {"Boarding", "Departed", "Departed", "Cancelled"};
                        while (r.next())
                            check(
                                    expected[r.getInt(1) - 1].equals(r.getString(2)),
                                    "Correct trip status " + r.getInt(1));
                    }
                    sql.executeUpdate(
                            "UPDATE trips SET"
                                + " departure_date=CURRENT_DATE,departure_time=CURRENT_TIME WHERE"
                                + " trip_id=1");
                    DepartureService.reconcile(c);
                    check(
                            queue.boarding().isEmpty(),
                            "Departure removes boarding entries without attendance requirement");
                    try (ResultSet r =
                            sql.executeQuery(
                                    "SELECT b.status,p.status,q.status FROM bookings b JOIN"
                                        + " payments p ON p.booking_id=b.booking_id JOIN"
                                        + " queue_entries q ON q.booking_id=b.booking_id WHERE"
                                        + " q.queue_entry_id="
                                            + missingId)) {
                        check(
                                r.next()
                                        && "No-show".equals(r.getString(1))
                                        && "Paid".equals(r.getString(2))
                                        && "No-show".equals(r.getString(3)),
                                "Unboarded passenger is No-show without changing paid payment");
                    }
                    try (ResultSet r =
                            sql.executeQuery(
                                    "SELECT b.status FROM bookings b JOIN queue_entries q ON"
                                        + " q.booking_id=b.booking_id WHERE q.queue_entry_id="
                                            + boardingId)) {
                        check(
                                r.next() && !"No-show".equals(r.getString(1)),
                                "Boarded passenger is preserved");
                    }
                    check(
                            queue.stations("Boarding").isEmpty(),
                            "Departure clears active boarding calls");
                    try (ResultSet r =
                            sql.executeQuery("SELECT trip_id FROM boarding_gates WHERE gate=1")) {
                        check(r.next() && r.getObject(1) == null, "Departure frees gate");
                    }
                    check(
                            "Departed".equals(new BusDao().departureStatuses().get(1)),
                            "Bus displays departed trip");
                    qpal.model.Trip departed = new TripDao().getTrip(1);
                    departed.setStatus("Scheduled");
                    try {
                        new TripDao()
                                .saveTripWithFare(departed, new java.math.BigDecimal("50"), true);
                        throw new AssertionError("Departed trip status change accepted");
                    } catch (TripDao.DepartedTripException expected) {
                        check(
                                "Trip is departed.".equals(expected.getMessage()),
                                "Departed warning");
                    }
                    sql.executeUpdate(
                            "INSERT INTO"
                                + " trips(trip_id,bus_id,route_id,departure_date,departure_time,available_seats,status)"
                                + " VALUES(5,1,1,CURRENT_DATE+INTERVAL 2"
                                + " DAY,'12:00:00',20,'Scheduled')");
                    check(
                            "Scheduled".equals(new BusDao().departureStatuses().get(1)),
                            "Future trip replaces old departure");
                    sql.executeUpdate("UPDATE trips SET status='Boarding' WHERE trip_id=5");
                    check(
                            "Boarding".equals(new BusDao().departureStatuses().get(1)),
                            "Bus follows boarding status");
                    sql.executeUpdate("UPDATE trips SET status='Cancelled' WHERE trip_id=5");
                    check(
                            "Cancelled".equals(new BusDao().departureStatuses().get(1)),
                            "Bus follows cancellation status");
                    DepartureService.reconcile(c);
                    try (ResultSet r =
                            sql.executeQuery("SELECT available_seats FROM trips WHERE trip_id=1")) {
                        r.next();
                        check(
                                r.getInt(1) == 18,
                                "Departure preserves seat accounting and is repeatable");
                    }
                }
                System.out.println("Automatic departure and boarding checks passed.");
            } finally {
                if (originalUrl == null) System.clearProperty("qpal.db.url");
                else System.setProperty("qpal.db.url", originalUrl);
                s.executeUpdate("DROP DATABASE " + schema);
            }
        }
    }
}
