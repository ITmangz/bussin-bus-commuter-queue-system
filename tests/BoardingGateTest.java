import java.sql.*;
import qpal.dao.*;
import qpal.util.DbConnection;

public class BoardingGateTest {
    interface Action {
        void run() throws Exception;
    }

    static void rejects(Action action) throws Exception {
        try {
            action.run();
            throw new AssertionError("Expected rejection");
        } catch (SQLException expected) {
        }
    }

    public static void main(String[] args) throws Exception {
        String name = "qpal_gate_test_" + Long.toUnsignedString(System.nanoTime());
        try (Connection admin = DbConnection.getConnection();
                Statement setup = admin.createStatement()) {
            setup.executeUpdate("CREATE DATABASE " + name);
            try {
                for (String table :
                        new String[] {
                            "trips",
                            "buses",
                            "routes",
                            "bookings",
                            "payments",
                            "queue_entries",
                            "booking_passengers",
                            "seat_reservations",
                            "queue_daily_counters",
                            "queue_boarding"
                        })
                    setup.executeUpdate(
                            "CREATE TABLE " + name + "." + table + " LIKE qpal." + table);
                System.setProperty("qpal.db.url", "jdbc:mysql://localhost:3306/" + name);
                try (Connection c = DbConnection.getConnection();
                        Statement s = c.createStatement()) {
                    s.executeUpdate(
                            "INSERT INTO buses"
                                + " VALUES(1,'TestBus1',40,40,'Available'),(2,'TestBus2',40,40,'Available'),(3,'TestBus3',40,40,'Available')");
                    s.executeUpdate("INSERT INTO routes VALUES(1,'A','B',50,'Active')");
                    for (int i = 1; i <= 3; i++) {
                        s.executeUpdate(
                                "INSERT INTO trips VALUES("
                                        + i
                                        + ","
                                        + i
                                        + ",1,CURRENT_DATE,'23:59:59',39,'Scheduled')");
                        s.executeUpdate(
                                "INSERT INTO bookings(booking_id,booking_reference,trip_id,status)"
                                    + " VALUES("
                                        + i
                                        + ",'TEST"
                                        + i
                                        + "',"
                                        + i
                                        + ",'Completed')");
                        s.executeUpdate(
                                "INSERT INTO booking_passengers VALUES("
                                        + i
                                        + ","
                                        + i
                                        + ","
                                        + i
                                        + ",'Test Person','Regular',50)");
                        s.executeUpdate(
                                "INSERT INTO"
                                    + " payments(payment_id,trip_id,booking_id,commuter_name,amount,status,paid_at)"
                                    + " VALUES("
                                        + i
                                        + ","
                                        + i
                                        + ","
                                        + i
                                        + ",'Test Person',50,'Paid',NOW())");
                        s.executeUpdate(
                                "INSERT INTO"
                                    + " queue_entries(queue_entry_id,booking_id,queue_date,queue_number,status)"
                                    + " VALUES("
                                        + i
                                        + ","
                                        + i
                                        + ",CURRENT_DATE,"
                                        + i
                                        + ",'Completed')");
                    }
                }
                var gates = new BoardingGateDao();
                var queue = new QueueDao();
                if (queue.boarding().size() != 3
                        || !queue.boarding().get(0).status().equals("Awaiting Gate"))
                    throw new AssertionError("Paid queues missing");
                gates.assign(1, 1);
                gates.assign(2, 2);
                rejects(() -> gates.assign(1, 3));
                rejects(() -> gates.assign(2, 1));
                queue.callBoarding(0, 1, false);
                queue.callBoarding(0, 2, false);
                if (queue.stations("Boarding").get(1) != 1
                        || queue.stations("Boarding").get(2) != 2)
                    throw new AssertionError("Mixed trips");
                rejects(() -> gates.close(1, true, false));
                gates.close(1, true, true);
                try (Connection c = DbConnection.getConnection();
                        Statement s = c.createStatement();
                        ResultSet r =
                                s.executeQuery(
                                        "SELECT b.status,p.status,t.status FROM bookings b JOIN"
                                            + " payments p ON p.booking_id=b.booking_id JOIN trips"
                                            + " t ON t.trip_id=b.trip_id WHERE b.booking_id=1")) {
                    r.next();
                    if (!r.getString(1).equals("No-show")
                            || !r.getString(2).equals("Paid")
                            || !r.getString(3).equals("Departed"))
                        throw new AssertionError("No-show settlement");
                }
                gates.assign(1, 3);
                gates.close(2, false, false);
                if (gates.waitingTrips().stream().noneMatch(t -> t.id() == 2))
                    throw new AssertionError("Released trip missing");
                System.out.println(
                        "PASS: paid queue transfer, exclusive gates, trip-specific calls, no-show"
                            + " confirmation preserves payment, departure, gate"
                            + " release/reassignment.");
            } finally {
                System.clearProperty("qpal.db.url");
                setup.executeUpdate("DROP DATABASE " + name);
            }
        }
    }
}
