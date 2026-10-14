import java.sql.*;
import java.util.*;
import qpal.dao.*;
import qpal.model.*;
import qpal.model.BookingData.*;
import qpal.util.DbConnection;

/** Exercises real booking, receipt, queue and bus DAO behavior in a disposable schema. */
public class PriorityBookingTest {
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    interface Action { void run() throws Exception; }
    static void rejects(Action action) throws Exception {
        try { action.run(); throw new AssertionError("Expected rejection"); }
        catch (SQLException expected) { }
    }
    static void sql(String sql) throws Exception {
        try (Connection c = DbConnection.getConnection(); Statement s = c.createStatement()) { s.executeUpdate(sql); }
    }
    static int queueId(QueueDao dao, Receipt receipt) throws Exception {
        return dao.today().stream().filter(r -> r.bookingId() == receipt.bookingId()).findFirst().orElseThrow().id();
    }
    public static void main(String[] args) throws Exception {
        String schema = "qpal_priority_test_" + Long.toUnsignedString(System.nanoTime());
        try (Connection admin = DbConnection.getConnection(); Statement setup = admin.createStatement()) {
            setup.executeUpdate("CREATE DATABASE " + schema);
            try {
                for (String table : List.of("buses", "routes", "trips", "bookings", "booking_passengers",
                        "seat_reservations", "payments", "queue_entries", "queue_daily_counters"))
                    setup.executeUpdate("CREATE TABLE " + schema + "." + table + " LIKE qpal." + table);
                System.setProperty("qpal.db.url", "jdbc:mysql://localhost:3306/" + schema);
                sql("INSERT INTO routes(route_id,origin,destination,fare,status) VALUES(1,'Test A','Test B',50,'Active')");
                BookingDao book = new BookingDao();
                BusDao buses = new BusDao();
                QueueDao queue = new QueueDao();
                for (SeatLayout layout : SeatLayout.values()) {
                    Bus bus = new Bus();
                    bus.setBusNumber("TEST-" + layout.code()); bus.setSeatCapacity(41);
                    bus.setAvailableSeats(41); bus.setBusStatus("Available"); bus.setSeatLayout(layout);
                    check(!buses.addBus(bus), "Incomplete rows rejected");
                    bus.setSeatCapacity(40); bus.setAvailableSeats(40);
                    check(buses.addBus(bus), "Add " + layout);
                    bus = buses.getAllBuses().stream().filter(b -> b.getBusNumber().equals("TEST-" + layout.code())).findFirst().orElseThrow();
                    int id = bus.getBusID();
                    sql("INSERT INTO trips(trip_id,bus_id,route_id,departure_date,departure_time,available_seats,status) VALUES("
                            + id + "," + id + ",1,CURRENT_DATE+INTERVAL 1 DAY,'12:00:00',40,'Scheduled')");
                    TripOption trip = book.availableTrips().stream().filter(t -> t.id() == id).findFirst().orElseThrow();
                    check(trip.layout() == layout, "Trip uses saved layout");
                    rejects(() -> book.book("BAD-" + id, trip, List.of(new Passenger("Regular", "Regular", 1)), "Cash"));
                    rejects(() -> book.book("BADST-" + id, trip, List.of(new Passenger("Student", "Student", 2)), "Cash"));
                    Receipt regular = book.book("REG-" + id, trip, List.of(new Passenger("Regular", "Regular", 6)), "Cash");
                    Receipt senior = book.book("SEN-" + id, trip, List.of(new Passenger("Senior", "Senior", 1)), "Cash");
                    Receipt pwd = book.book("PWD-" + id, trip, List.of(new Passenger("Companion", "Regular", 7), new Passenger("PWD", "PWD", 2)), "Cash");
                    check(book.receipt(pwd.bookingId()).seats().equals(layout.label(7) + ", 1B"), "Mixed receipt seat labels");
                    check(queue.today().stream().filter(r -> r.bookingId() == pwd.bookingId()).findFirst().orElseThrow().priority(), "Mixed group gets priority");
                    Bus change = buses.getBus(id);
                    change.setSeatCapacity(41);
                    rejects(() -> buses.updateBusDetails(change));
                    change.setSeatCapacity(40);
                    change.setSeatLayout(layout == SeatLayout.TWO_TWO ? SeatLayout.THREE_TWO : SeatLayout.TWO_TWO);
                    rejects(() -> buses.updateBusDetails(change));
                    if (layout == SeatLayout.THREE_TWO) {
                        int r = queueId(queue, regular), s = queueId(queue, senior), p = queueId(queue, pwd);
                        check(queue.today().stream().map(QueueRow::id).toList().equals(List.of(s, p, r)), "Priority FIFO list");
                        rejects(() -> queue.act(r, "Recall"));
                        queue.act(0, "Call Next Queue", 1);
                        check(queue.stations("Payment").get(1) == s, "Senior called first");
                        rejects(() -> queue.act(0, "Call Next Queue", 1));
                        queue.act(0, "Call Next Queue", 2);
                        check(queue.stations("Payment").get(2) == p, "PWD next, same priority tier");
                        queue.act(s, "Skip Queue", 1);
                        queue.act(0, "Call Next Queue", 1);
                        check(queue.stations("Payment").get(1) == r, "Waiting before skipped retry");
                        sql("UPDATE queue_entries SET status='Completed'");
                        sql("UPDATE payments SET status='Paid',paid_at=NOW()");
                        sql("DELETE FROM queue_stations");
                        check(queue.boarding().stream().map(QueueRow::id).toList().equals(List.of(s, p, r)), "Boarding priority FIFO");
                        new BoardingGateDao().assign(1, id);
                        rejects(() -> queue.callBoarding(r, 1, false));
                        queue.callBoarding(0, 1, false);
                        check(queue.stations("Boarding").get(1) == s, "Gate calls priority first");
                        rejects(() -> queue.callBoarding(0, 1, false));
                    }
                    sql("UPDATE trips SET status='Departed' WHERE trip_id=" + id);
                    check(buses.updateBusDetails(change), "Historical bookings allow layout change");
                    check(book.receipt(regular.bookingId()).seats().equals(layout.label(6)), "Historical receipt preserves layout");
                    // Prevent previous layout's waiting queues from affecting the next fixture.
                    sql("UPDATE queue_entries SET status='Completed'");
                    sql("DELETE FROM queue_stations");
                }
                check(new QueueDao().revenue().rows().size() > 0, "Revenue labels load with booking layout");
                System.out.println("PASS: three layouts, priority seat enforcement, mixed groups, historical labels, payment and boarding priority FIFO, skip handling, active bus edit protection.");
            } finally {
                System.clearProperty("qpal.db.url");
                setup.executeUpdate("DROP DATABASE " + schema);
            }
        }
    }
}
