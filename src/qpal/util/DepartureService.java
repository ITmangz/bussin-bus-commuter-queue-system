package qpal.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Reconciles overdue departures while any application database session is in use. */
public final class DepartureService {
    private static final AtomicBoolean started = new AtomicBoolean();
    private DepartureService() {}

    public static void start() {
        if (!started.compareAndSet(false, true)) return;
        var executor = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "scheduled-departures");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleWithFixedDelay(() -> {
            try (Connection connection = DbConnection.getConnection()) {
                reconcile(connection);
            } catch (SQLException ex) {
                System.err.println("Automatic departure update failed; retrying: " + ex.getMessage());
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public static void reconcile(Connection connection) throws SQLException {
        try (var statement = connection.prepareStatement(
                "UPDATE trips SET status='Departed' WHERE status IN ('Scheduled','Boarding') "
                + "AND TIMESTAMP(departure_date,departure_time)<=NOW()")) {
            statement.executeUpdate();
        }
    }
}
