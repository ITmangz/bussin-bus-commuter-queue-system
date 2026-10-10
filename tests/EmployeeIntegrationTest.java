import java.sql.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import qpal.dao.*;
import qpal.model.*;
import qpal.model.BookingData.Passenger;
import qpal.util.DbConnection;

public class EmployeeIntegrationTest {
    interface Action {
        void run() throws Exception;
    }

    static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    static void rejects(Action action) throws Exception {
        try {
            action.run();
            throw new AssertionError("Expected station rejection");
        } catch (SQLException | IllegalArgumentException expected) {
        }
    }

    static void signIn(Account account, EmployeeStationDao.Session session) {
        ActivityLogDao.setCurrentAccount(account);
        EmployeeStationDao.activate(session);
    }

    static void sql(String query) throws Exception {
        try (Connection c = DbConnection.getConnection();
                Statement s = c.createStatement()) {
            s.executeUpdate(query);
        }
    }

    public static void main(String[] args) throws Exception {
        String schema = "qpal_employee_test_" + Long.toUnsignedString(System.nanoTime());
        String oldUrl = System.getProperty("qpal.db.url");
        try (Connection setup = DbConnection.getConnection();
                Statement s = setup.createStatement()) {
            s.executeUpdate("CREATE DATABASE " + schema);
            try {
                for (String table :
                        new String[] {
                            "accounts",
                            "activity_logs",
                            "buses",
                            "routes",
                            "trips",
                            "bookings",
                            "booking_passengers",
                            "seat_reservations",
                            "payments",
                            "queue_entries",
                            "queue_daily_counters"
                        })
                    s.executeUpdate("CREATE TABLE " + schema + "." + table + " LIKE qpal." + table);
                System.setProperty("qpal.db.url", "jdbc:mysql://localhost:3306/" + schema);
                sql(
                        "INSERT INTO accounts(id,name,email,password,role,status)"
                            + " VALUES(1,'One','one@example.test','x','Employee','Active'),(2,'Two','two@example.test','x','Employee','Active'),(3,'Three','three@example.test','x','Employee','Active'),(4,'Four','four@example.test','x','Employee','Active')");
                Account one = new Account(1, "One", "one@example.test", "", "Employee", "Active");
                Account two = new Account(2, "Two", "two@example.test", "", "Employee", "Active");
                Account three =
                        new Account(3, "Three", "three@example.test", "", "Employee", "Active");
                Account four =
                        new Account(4, "Four", "four@example.test", "", "Employee", "Active");
                var stations = new EmployeeStationDao();
                var counter1 = new EmployeeStation("Payment", 1);
                var counter2 = new EmployeeStation("Payment", 2);
                var first = stations.claim(one, counter1);
                rejects(() -> stations.claim(two, counter1));
                rejects(() -> stations.claim(one, counter2));
                stations.heartbeat(first);
                var second = stations.claim(two, counter2);
                check(stations.occupied().size() == 2, "Exclusive counter leases");
                // Simultaneous claims for the same gate have exactly one winner.
                ExecutorService pool = Executors.newFixedThreadPool(2);
                CountDownLatch start = new CountDownLatch(1);
                Callable<EmployeeStationDao.Session> a =
                        () -> {
                            start.await();
                            try {
                                return stations.claim(three, new EmployeeStation("Boarding", 1));
                            } catch (SQLException ex) {
                                return null;
                            }
                        };
                Callable<EmployeeStationDao.Session> b =
                        () -> {
                            start.await();
                            try {
                                return stations.claim(four, new EmployeeStation("Boarding", 1));
                            } catch (SQLException ex) {
                                return null;
                            }
                        };
                var fa = pool.submit(a);
                var fb = pool.submit(b);
                start.countDown();
                var sa = fa.get();
                var sb = fb.get();
                pool.shutdown();
                check((sa == null) != (sb == null), "Concurrent claim has one winner");
                stations.release(sa != null ? sa : sb);
                sql(
                        "INSERT INTO"
                            + " buses(bus_id,bus_number,seat_capacity,available_seats,bus_status)"
                            + " VALUES(1,'TEST',20,20,'Available')");
                sql(
                        "INSERT INTO routes(route_id,origin,destination,fare,status)"
                            + " VALUES(1,'A','B',50,'Active')");
                sql(
                        "INSERT INTO"
                            + " trips(trip_id,bus_id,route_id,departure_date,departure_time,available_seats,status)"
                            + " VALUES(1,1,1,CURRENT_DATE+INTERVAL 1"
                            + " DAY,'12:00:00',20,'Scheduled')");
                var bookings = new BookingDao();
                var queues = new QueueDao();
                var payments = new QueuePaymentDao();
                var trip = bookings.availableTrips().get(0);
                bookings.book(
                        "EMPLOYEE-TEST-1",
                        trip,
                        List.of(
                                new Passenger("Passenger A", "Regular", 1),
                                new Passenger("Passenger B", "Regular", 2)),
                        "Cash");
                bookings.book(
                        "EMPLOYEE-TEST-2",
                        trip,
                        List.of(new Passenger("Passenger C", "Regular", 3)),
                        "Cash");
                signIn(one, first);
                rejects(() -> queues.act(0, "Call Next Queue", 2));
                queues.act(0, "Call Next Queue", 1);
                int queue = queues.stations("Payment").get(1);
                var summary = new EmployeeDashboardDao().load(first);
                check(
                        summary.transactions() == 0 && summary.sharedWaiting() == 1,
                        "Unfinished booking is not a transaction");
                rejects(() -> payments.pay(queue, 2, new BigDecimal("100")));
                rejects(() -> payments.pay(queue, 1, new BigDecimal("99")));
                check(
                        new EmployeeDashboardDao().load(first).collected().signum() == 0,
                        "Failed payment not attributed");
                payments.pay(queue, 1, new BigDecimal("200"));
                check(
                        new EmployeeDashboardDao()
                                        .load(first)
                                        .collected()
                                        .compareTo(new BigDecimal("100"))
                                == 0,
                        "Revenue is fare, not cash tender");
                rejects(() -> payments.pay(queue, 1, new BigDecimal("200")));
                signIn(two, second);
                rejects(() -> queues.act(queue, "Complete", 2));
                rejects(() -> payments.printed(queue, false));
                check(
                        new EmployeeDashboardDao()
                                .stationQueues(counter2).stream().noneMatch(r -> r.id() == queue),
                        "Other counter queue hidden");
                check(
                        new EmployeeDashboardDao().load(second).collected().signum() == 0,
                        "Employee totals isolated");
                signIn(one, first);
                rejects(() -> queues.act(queue, "Complete", 1));
                check(
                        new EmployeeDashboardDao().load(first).commuters() == 0,
                        "Failed completion not counted");
                payments.printed(queue, false);
                payments.printed(queue, true);
                queues.act(queue, "Complete", 1);
                check(
                        new EmployeeDashboardDao().load(first).commuters() == 2,
                        "Counts passengers, not bookings");
                check(
                        new ActivityLogDao()
                                .getAllActivities(one).stream()
                                        .anyMatch(
                                                l ->
                                                        l.getDescription()
                                                                .contains("2 boarding ticket(s)")),
                        "Print log includes boarding ticket quantity");
                check(
                        new EmployeeDashboardDao().load(first).transactions() == 1,
                        "Completed booking counts as one transaction");
                var gate = stations.claim(three, new EmployeeStation("Boarding", 1));
                signIn(three, gate);
                var gates = new BoardingGateDao();
                rejects(() -> gates.assign(2, trip.id()));
                gates.assign(1, trip.id());
                sql(
                        "INSERT INTO"
                            + " buses(bus_id,bus_number,seat_capacity,available_seats,bus_status)"
                            + " VALUES(2,'SECOND',30,30,'Available')");
                sql(
                        "INSERT INTO"
                            + " trips(trip_id,bus_id,route_id,departure_date,departure_time,available_seats,status)"
                            + " VALUES(2,2,1,CURRENT_DATE+INTERVAL 1"
                            + " DAY,'14:00:00',30,'Scheduled')");
                signIn(null, null);
                gates.assign(2, 2);
                signIn(three, gate);
                var gateSummary = new EmployeeDashboardDao().load(gate);
                check(
                        gateSummary.activeBoarding().size() == 2,
                        "Gate employee sees all active boarding buses");
                check(
                        gateSummary.activeBoarding().stream()
                                .anyMatch(
                                        t ->
                                                t.bus().equals("TEST")
                                                        && t.capacity() == 20
                                                        && t.occupied() == 3),
                        "Shared boarding cards receive real seat totals");
                check(
                        gateSummary.stationTrip() != null
                                && gateSummary.stationTrip().contains("TEST"),
                        "Queue status retains only assigned trip");
                check(
                        new EmployeeDashboardDao().load(first).activeBoarding().size() == 2,
                        "Counter employee sees all boarding buses");
                check(
                        new EmployeeDashboardDao().stationQueues(gate.station()).size() == 1,
                        "Gate queues filtered to trip");
                check(
                        new EmployeeDashboardDao()
                                .stationQueues(new EmployeeStation("Boarding", 2))
                                .isEmpty(),
                        "Other gate excludes trip");
                queues.callBoarding(0, 1, false);
                rejects(() -> queues.callBoarding(0, 2, false));
                queues.recallBoarding(queue, 1);
                rejects(() -> queues.recallBoarding(queue, 2));
                queues.completeBoarding(queue);
                rejects(() -> queues.recallBoarding(queue, 1));
                rejects(() -> queues.completeBoarding(queue));
                var boarded = new EmployeeDashboardDao().load(gate);
                check(
                        boarded.commuters() == 2
                                && boarded.collected().signum() == 0
                                && boarded.transactions() == 1,
                        "Boarding service credited once, no payment revenue");
                var logs = new ActivityLogDao().getAllActivities(three);
                check(
                        !logs.isEmpty()
                                && logs.stream()
                                        .allMatch(l -> l.getEmail().equals(three.getEmail())),
                        "Only own activity logs");
                sql(
                        "UPDATE employee_stations SET expires_at=NOW()-INTERVAL 1 SECOND WHERE"
                            + " kind='Payment' AND station=1");
                signIn(one, first);
                rejects(() -> queues.act(0, "Call Next Queue", 1));
                rejects(() -> stations.heartbeat(first));
                var replacement = stations.claim(four, counter1);
                stations.release(first);
                stations.heartbeat(replacement);
                sql("UPDATE accounts SET status='Inactive' WHERE id=4");
                rejects(() -> stations.heartbeat(replacement));
                signIn(four, replacement);
                rejects(() -> queues.act(0, "Call Next Queue", 1));
                stations.release(replacement);
                stations.release(second);
                stations.release(gate);
                check(stations.occupied().isEmpty(), "Logout releases all stations");
                var summaries = new EmployeeSummaryDao();
                summaries.logout(first);
                summaries.logout(first);
                var logoutLogs =
                        new ActivityLogDao()
                                .getAllActivities(one).stream()
                                        .filter(l -> l.getAction().equals("Logout"))
                                        .toList();
                check(
                        logoutLogs.size() == 1
                                && logoutLogs
                                        .get(0)
                                        .getDescription()
                                        .contains("Session collected: PHP 100.00")
                                && logoutLogs
                                        .get(0)
                                        .getDescription()
                                        .contains("Today's total: PHP 100.00"),
                        "Logout credits fare once, excluding change");
                var again = stations.claim(one, counter1);
                signIn(one, again);
                check(
                        new EmployeeDashboardDao()
                                        .load(again)
                                        .collected()
                                        .compareTo(new BigDecimal("100"))
                                == 0,
                        "Same-day login retains today's total");
                summaries.logout(again);
                check(
                        new ActivityLogDao()
                                .getAllActivities(one).stream()
                                        .anyMatch(
                                                l ->
                                                        l.getDescription()
                                                                        .contains(
                                                                                "Session collected:"
                                                                                    + " PHP 0.00")
                                                                && l.getDescription()
                                                                        .contains(
                                                                                "Today's total: PHP"
                                                                                    + " 100.00")),
                        "New session starts at zero without resetting daily collections");
                sql(
                        "UPDATE employee_queue_work SET created_at=CURRENT_DATE-INTERVAL 1 DAY"
                            + " WHERE account_id=1");
                summaries.reconcile();
                summaries.reconcile();
                var daily =
                        new ActivityLogDao()
                                .getAllActivities(one).stream()
                                        .filter(l -> l.getAction().equals("Daily Summary"))
                                        .toList();
                check(
                        daily.size() == 1
                                && daily.get(0).getDescription().contains("PHP 100.00")
                                && daily.get(0).getDescription().contains("Commuters served: 2"),
                        "Catch-up creates one final daily summary");
                check(
                        new EmployeeDashboardDao().load(again).collected().signum() == 0,
                        "New day displays zero without deleting historical earnings");
                sql(
                        "UPDATE employee_work_sessions SET started_at=CURRENT_DATE-INTERVAL 3"
                            + " DAY,ended_at=CURRENT_DATE-INTERVAL 2 DAY WHERE token='"
                                + second.token()
                                + "'");
                summaries.reconcile();
                check(
                        new ActivityLogDao()
                                        .getAllActivities(two).stream()
                                                .filter(l -> l.getAction().equals("Daily Summary"))
                                                .count()
                                == 2,
                        "Zero-collection days catch up too");
                sql(
                        "INSERT INTO"
                            + " employee_queue_work(event,queue_entry_id,account_id,kind,station,amount,created_at)"
                            + " VALUES('Payment',999999,1,'Payment',1,25,CURRENT_DATE-INTERVAL 5"
                            + " DAY)");
                sql("RENAME TABLE activity_logs TO summary_test_logs");
                try {
                    rejects(summaries::reconcile);
                } finally {
                    sql("RENAME TABLE summary_test_logs TO activity_logs");
                }
                summaries.reconcile();
                summaries.reconcile();
                check(
                        new ActivityLogDao()
                                        .getAllActivities(one).stream()
                                                .filter(
                                                        l ->
                                                                l.getAction()
                                                                                .equals(
                                                                                        "Daily"
                                                                                            + " Summary")
                                                                        && l.getDescription()
                                                                                .contains(
                                                                                        "PHP 25.00"))
                                                .count()
                                == 1,
                        "Failed log insertion rolls back the daily marker and safely retries");
                System.out.println(
                        "PASS: session logout totals, same-day re-login, daily rollover, catch-up,"
                            + " zero-collection days, duplicate prevention, and log-failure"
                            + " retry.");
                System.out.println(
                        "PASS: concurrent/exclusive claims, duplicate employee protection, station"
                            + " authorization, payment rollback/attribution, own queue scope,"
                            + " passenger counts, gate completion, private logs, expiry, stale"
                            + " release, inactive account rejection.");
            } finally {
                signIn(null, null);
                if (oldUrl == null) System.clearProperty("qpal.db.url");
                else System.setProperty("qpal.db.url", oldUrl);
                s.executeUpdate("DROP DATABASE " + schema);
            }
        }
    }
}
