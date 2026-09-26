package qpal.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import qpal.util.DbConnection;

public class DashboardDao {
    public static class Summary {
        public int available;
        public int maintenance;
        public int inactive;
        public int total;
        public int commuters;
        public String currentQueue = "—";
        public final List<String[]> departures = new ArrayList<>();
    }

    public Summary loadSummary() throws SQLException {
        Summary summary = new Summary();
        try (Connection connection = DbConnection.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM booking_passengers bp JOIN queue_entries q ON q.booking_id=bp.booking_id "
                    + "WHERE q.queue_date=CURRENT_DATE AND q.status<>'Cancelled'"); ResultSet rows = statement.executeQuery()) {
                rows.next(); summary.commuters = rows.getInt(1);
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT queue_number FROM queue_entries WHERE queue_date=CURRENT_DATE AND status='Serving' ORDER BY queue_number LIMIT 1");
                    ResultSet rows = statement.executeQuery()) {
                if (rows.next()) summary.currentQueue = String.format(java.util.Locale.ROOT,"P%03d",rows.getInt(1));
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT bus_status, COUNT(*) AS total FROM buses GROUP BY bus_status");
                 ResultSet rows = statement.executeQuery()) {
                while(rows.next()) {
                    int count = rows.getInt("total");
                    summary.total += count;
                    String status = rows.getString("bus_status");
                    if("Available".equalsIgnoreCase(status)) {

                        summary.available += count;
                    }
                    if("Maintenance".equalsIgnoreCase(status)) {

                        summary.maintenance += count;
                    }
                    if("Inactive".equalsIgnoreCase(status)) {

                        summary.inactive += count;
                    }
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT b.bus_number, r.origin, r.destination, t.departure_date, t.departure_time "
                    + "FROM trips t JOIN buses b ON b.bus_id = t.bus_id "
                    + "JOIN routes r ON r.route_id = t.route_id "
                    + "WHERE t.status IN ('Scheduled', 'Boarding') "
                    + "AND TIMESTAMP(t.departure_date, t.departure_time) >= NOW() "
                    + "ORDER BY t.departure_date, t.departure_time LIMIT 3");
                 ResultSet rows = statement.executeQuery()) {
                while(rows.next()) {
                    summary.departures.add(new String[] {
                        rows.getString("origin") + " → " + rows.getString("destination"),
                        rows.getString("bus_number") + " • " + rows.getString("departure_date")
                            + "  " + rows.getString("departure_time")
                    });
                }
            }
        }
        return summary;
    }
}
