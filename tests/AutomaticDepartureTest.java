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
        try (Connection setup = DbConnection.getConnection(); Statement s = setup.createStatement()) {
            s.executeUpdate("CREATE DATABASE " + schema);
            try {
                for (String table : List.of("buses","routes","trips","bookings","booking_passengers",
                        "seat_reservations","payments","queue_entries","queue_daily_counters"))
                    s.executeUpdate("CREATE TABLE " + schema + "." + table + " LIKE qpal." + table);
                System.setProperty("qpal.db.url", "jdbc:mysql://localhost:3306/" + schema);
                try (Connection c = DbConnection.getConnection(); Statement sql = c.createStatement()) {
                    sql.executeUpdate("INSERT INTO buses(bus_id,bus_number,seat_capacity,available_seats,bus_status) VALUES(1,'TEST',20,20,'Available')");
                    sql.executeUpdate("INSERT INTO routes(route_id,origin,destination,fare,status) VALUES(1,'A','B',50,'Active')");
                    sql.executeUpdate("INSERT INTO trips(trip_id,bus_id,route_id,departure_date,departure_time,available_seats,status) VALUES"
                            + "(1,1,1,CURRENT_DATE+INTERVAL 1 DAY,'12:00:00',20,'Boarding'),"
                            + "(2,1,1,CURRENT_DATE-INTERVAL 1 DAY,'12:00:00',20,'Boarding'),"
                            + "(3,1,1,CURRENT_DATE-INTERVAL 1 DAY,'13:00:00',20,'Scheduled'),"
                            + "(4,1,1,CURRENT_DATE-INTERVAL 1 DAY,'14:00:00',20,'Cancelled')");
                    BookingDao booking = new BookingDao();
                    TripOption trip = booking.availableTrips().stream().filter(t -> t.id()==1).findFirst().orElseThrow();
                    booking.book("BOARDING-TEST",trip,List.of(new Passenger("Passenger","Regular",1)),"Cash");
                    QueueDao queue = new QueueDao();
                    check(queue.boarding().isEmpty(),"Unpaid passengers excluded");
                    queue.act(queue.today().get(0).id(),"Mark as Paid");
                    check(queue.boarding().size()==1,"Paid passenger appears in boarding queue");
                    sql.executeUpdate("UPDATE queue_entries SET queue_date=CURRENT_DATE-INTERVAL 1 DAY");
                    check(queue.boarding().size()==1,"Earlier bookings remain in boarding queue");
                    int boardingId = queue.boarding().get(0).id();
                    queue.completeBoarding(boardingId);
                    check(queue.boarding().isEmpty(),"Completed boarding is removed from the list");
                    try { queue.completeBoarding(boardingId); throw new AssertionError("Duplicate boarding accepted"); }
                    catch (SQLException expected) { }
                    booking.book("BOARDING-SECOND",trip,List.of(new Passenger("Second","Regular",2)),"Cash");
                    queue.act(queue.today().get(0).id(),"Mark as Paid");
                    check(queue.boarding().size()==1,"Other boarding queues remain actionable");
                    DepartureService.reconcile(c);
                    try (ResultSet r = sql.executeQuery("SELECT trip_id,status,available_seats FROM trips ORDER BY trip_id")) {
                        String[] expected = {"Boarding","Departed","Departed","Cancelled"};
                        while (r.next()) check(expected[r.getInt(1)-1].equals(r.getString(2)),"Correct trip status " + r.getInt(1));
                    }
                    sql.executeUpdate("UPDATE trips SET departure_date=CURRENT_DATE,departure_time=CURRENT_TIME WHERE trip_id=1");
                    DepartureService.reconcile(c);
                    check(queue.boarding().isEmpty(),"Departure removes boarding entries without attendance requirement");
                    check("Departed".equals(new BusDao().departureStatuses().get(1)),"Bus displays departed trip");
                    DepartureService.reconcile(c);
                    try (ResultSet r = sql.executeQuery("SELECT available_seats FROM trips WHERE trip_id=1")) {
                        r.next(); check(r.getInt(1)==18,"Departure preserves seat accounting and is repeatable");
                    }
                }
                System.out.println("Automatic departure and boarding checks passed.");
            } finally {
                if (originalUrl == null) System.clearProperty("qpal.db.url");
                else System.setProperty("qpal.db.url",originalUrl);
                s.executeUpdate("DROP DATABASE " + schema);
            }
        }
    }
}
