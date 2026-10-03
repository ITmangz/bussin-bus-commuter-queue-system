package qpal.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Releases unpaid reservations at the cutoff; departures require staff confirmation. */
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
                System.err.println("Payment deadline update failed; retrying: " + ex.getMessage());
            }
        }, 0, 5, TimeUnit.SECONDS);
        executor.scheduleWithFixedDelay(() -> {
            try {
                new qpal.dao.EmployeeSummaryDao().reconcile();
            } catch (SQLException ex) {
                System.err.println("Employee daily summary failed; retrying: " + ex.getMessage());
            }
        }, 0, 60, TimeUnit.SECONDS);
    }

    public static void reconcile(Connection connection) throws SQLException {
        qpal.dao.PaymentDeadlineDao.expire(connection);
    }
}
