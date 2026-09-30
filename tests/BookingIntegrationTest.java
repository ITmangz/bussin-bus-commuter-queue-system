import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import qpal.dao.*;
import qpal.model.BookingData.*;
import qpal.util.DbConnection;

/** Run only against a disposable schema copied from qpal, never the live database. */
public class BookingIntegrationTest {
    static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    static void sql(String sql) throws Exception {
        try (Connection c = DbConnection.getConnection(); Statement s = c.createStatement()) { s.executeUpdate(sql); }
    }
    static int count(String sql) throws Exception {
        try (Connection c = DbConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            r.next(); return r.getInt(1);
        }
    }
    interface Checked { void run() throws Exception; }
    static void rejects(Checked action) throws Exception {
        try { action.run(); throw new AssertionError("Expected request to fail"); }
        catch (SQLException expected) { }
    }
    static String reference() { return UUID.randomUUID().toString().replace("-", "").substring(0, 29); }
    static List<Passenger> passengers(int... seats) {
        List<Passenger> result = new ArrayList<>();
        for (int seat : seats) result.add(new Passenger("Test Passenger " + seat, "Regular", seat));
        return result;
    }
    public static void main(String[] args) throws Exception {
        check(System.getProperty("qpal.db.url", "").matches("jdbc:mysql://localhost:3306/qpal_integration_test_[a-z0-9]+"),
                "Refusing to run outside a disposable test database");
        sql("INSERT INTO buses(bus_id,bus_number,seat_capacity,available_seats,bus_status) VALUES(1,'TEST',20,20,'Available')");
        sql("INSERT INTO routes(route_id,origin,destination,fare,status) VALUES(1,'Test A','Test B',50,'Active')");
        sql("INSERT INTO trips(trip_id,bus_id,route_id,departure_date,departure_time,available_seats,status) VALUES"
                + "(1,1,1,CURRENT_DATE+INTERVAL 1 DAY,'12:00:00',20,'Scheduled'),"
                + "(2,1,1,CURRENT_DATE-INTERVAL 1 DAY,'12:00:00',20,'Scheduled'),"
                + "(3,1,1,CURRENT_DATE+INTERVAL 1 DAY,'13:00:00',20,'Cancelled')");
        BookingDao dao = new BookingDao();
        check(dao.availableTrips().size() == 1, "Only future bookable trips should appear");
        TripOption trip = dao.availableTrips().get(0);
        String ref = reference();
        Receipt receipt = dao.book(ref, trip, passengers(1,2), "Cash");
        check(receipt.total().compareTo(new BigDecimal("100.00")) == 0, "Fare total");
        check(receipt.paymentStatus().equals("Pending"), "Payment must not auto-pay");
        check(dao.occupiedSeats(1).equals(Set.of(1,2)), "Seats saved");
        check(dao.book(ref, trip, passengers(1,2), "Cash").bookingId() == receipt.bookingId(), "Repeat submission must be idempotent");
        check(count("SELECT available_seats FROM trips WHERE trip_id=1") == 18, "Availability changes once");
        rejects(() -> dao.book(reference(), trip, passengers(3,1), "Cash"));
        check(count("SELECT COUNT(*) FROM bookings") == 1, "Failed seat reservation rolls booking back");
        check(!dao.occupiedSeats(1).contains(3), "Earlier seats from failed booking roll back");
        check(count("SELECT COUNT(*) FROM payments") == 1, "No payment for failed booking");
        rejects(() -> dao.book(reference(), trip, passengers(21), "Cash"));
        sql("UPDATE routes SET fare=60 WHERE route_id=1");
        rejects(() -> dao.book(reference(), trip, passengers(4), "Cash"));
        sql("UPDATE routes SET fare=50 WHERE route_id=1");

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch gate = new CountDownLatch(1);
            List<Future<Boolean>> race = new ArrayList<>();
            for (int i = 0; i < 2; i++) race.add(pool.submit(() -> {
                gate.await();
                try { dao.book(reference(), trip, passengers(5), "GCash"); return true; }
                catch (SQLException expected) { return false; }
            }));
            gate.countDown();
            int wins = 0;
            for (Future<Boolean> f : race) if (f.get(15, TimeUnit.SECONDS)) wins++;
            check(wins == 1, "Concurrent kiosks must not double-book a seat");
            Future<Receipt> a = pool.submit(() -> dao.book(reference(), trip, passengers(6), "Card"));
            Future<Receipt> b = pool.submit(() -> dao.book(reference(), trip, passengers(7), "Cash"));
            check(a.get(15, TimeUnit.SECONDS).queueNumber() != b.get(15, TimeUnit.SECONDS).queueNumber(), "Daily queues unique");
        } finally { pool.shutdownNow(); }
        check(count("SELECT COUNT(*) FROM bookings") == 4, "Expected four successful bookings");
        check(count("SELECT available_seats FROM trips WHERE trip_id=1") == 15, "Availability matches reservations");
        QueueDao queue = new QueueDao();
        check(queue.today().size() == 4, "Admin sees kiosk bookings");
        int first = queue.today().get(0).id();
        queue.act(0, "Call Next Queue");
        rejects(() -> queue.act(0, "Call Next Queue"));
        rejects(() -> queue.act(first, "Complete"));
        queue.act(first, "Mark as Paid");
        rejects(() -> queue.act(first, "Mark as Paid"));
        rejects(() -> queue.act(first, "Complete"));
        new QueuePaymentDao().printed(first,false);
        new QueuePaymentDao().printed(first,true);
        queue.act(first, "Complete");
        check(queue.revenue().todayRevenue().compareTo(new BigDecimal("100")) == 0, "Only collected payments count as revenue");
        check(queue.revenue().paidPassengers() == 2, "Paid passenger count");
        check(dao.receipt(receipt.bookingId()).paymentStatus().equals("Paid"), "Reprint reflects payment");
        queue.act(0, "Call Next Queue");
        int second = queue.today().get(1).id();
        queue.act(second, "Skip Queue");
        queue.act(second, "Recall");
        check(queue.today().get(1).status().equals("Serving"), "Skipped queue can be recalled");
        check(new DashboardDao().loadSummary().commuters == 5, "Dashboard commuter count");
        qpal.model.Trip edit = new TripDao().getTrip(1);
        edit.setAvailableSeats(20);
        check(new TripDao().saveTripWithFare(edit, new BigDecimal("50"), true), "Booked trip status edit");
        check(count("SELECT available_seats FROM trips WHERE trip_id=1") == 15, "Admin edit must not reset booked seats");
        edit.setAvailableSeats(20);
        check(new TripDao().updateTrip(edit), "Alternate trip update path");
        check(count("SELECT available_seats FROM trips WHERE trip_id=1") == 15, "Alternate edit preserves availability");
        qpal.model.Bus bus = new BusDao().getBus(1);
        bus.setSeatCapacity(10);
        check(!new BusDao().updateBus(bus), "Cannot change capacity on a booked bus");
        BookingUiSmokeTest.run(trip, receipt);
        AdminManagementIntegrationTest.run();
        System.out.println("PASS: booking, rollback, idempotency, concurrent seats/queues, payments, queue actions, dashboard and trip-edit protection");
        System.exit(0); // Stop the existing kiosk clock timers created by the Swing smoke test.
    }
}
