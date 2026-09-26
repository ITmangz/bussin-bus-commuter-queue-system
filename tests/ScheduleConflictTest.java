import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import qpal.dao.TripDao;
import qpal.model.Trip;
import qpal.util.DbConnection;

/** Connection-local temporary tables shadow live tables; no persistent data is changed. */
public class ScheduleConflictTest {
    private static final Method CHECK;
    static {
        try {
            CHECK = TripDao.class.getDeclaredMethod("checkBusSchedule", Connection.class, Trip.class, boolean.class);
            CHECK.setAccessible(true);
        } catch (Exception ex) { throw new ExceptionInInitializerError(ex); }
    }

    private static void verify(Connection conn, int bus, int id, String date, String time,
            String status, boolean conflict) throws Exception {
        Trip trip = new Trip();
        trip.setBusID(bus);
        trip.setTripID(id);
        trip.setDepartureDate(date);
        trip.setDepartureTime(time);
        trip.setStatus(status);
        boolean rejected = false;
        try { CHECK.invoke(new TripDao(), conn, trip, id != 0); }
        catch (InvocationTargetException ex) {
            if (!(ex.getCause() instanceof TripDao.ScheduleConflictException)) throw ex;
            rejected = true;
            if (!ex.getCause().getMessage().contains("6 hours")) throw new AssertionError("Missing explanation");
        }
        if (rejected != conflict) throw new AssertionError("Unexpected result for " + date + " " + time);
    }

    public static void main(String[] args) throws Exception {
        try (Connection conn = DbConnection.getConnection(); Statement sql = conn.createStatement()) {
            sql.executeUpdate("CREATE TEMPORARY TABLE buses (bus_id INT PRIMARY KEY) ENGINE=InnoDB");
            sql.executeUpdate("CREATE TEMPORARY TABLE trips (trip_id INT PRIMARY KEY, bus_id INT, "
                    + "departure_date DATE, departure_time TIME, status VARCHAR(30)) ENGINE=InnoDB");
            sql.executeUpdate("INSERT INTO buses VALUES (1),(2)");
            sql.executeUpdate("INSERT INTO trips VALUES (1,1,'2026-10-01','22:00:00','Scheduled'),"
                    + "(2,1,'2026-10-03','12:00:00','Cancelled')");
            conn.setAutoCommit(false);
            verify(conn,1,0,"2026-10-01","22:00:00","Scheduled",true);
            verify(conn,1,0,"2026-10-02","03:59:59","Scheduled",true);
            verify(conn,1,0,"2026-10-02","04:00:00","Scheduled",false);
            verify(conn,1,0,"2026-10-01","16:00:01","Scheduled",true);
            verify(conn,1,0,"2026-10-01","16:00:00","Scheduled",false);
            verify(conn,2,0,"2026-10-01","22:00:00","Scheduled",false);
            verify(conn,1,1,"2026-10-01","22:00:00","Scheduled",false);
            verify(conn,1,0,"2026-10-03","12:00:00","Scheduled",false);
            verify(conn,1,0,"2026-10-01","22:00:00","Cancelled",false);
            conn.rollback();
        }
        System.out.println("Schedule conflict checks passed (9 cases).");
    }
}
