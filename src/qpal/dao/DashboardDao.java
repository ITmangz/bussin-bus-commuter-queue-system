package qpal.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import qpal.util.DbConnection;

public class DashboardDao {
    public record BoardingTrip(
            String bus, String route, String departure, int expected, int capacity, int boarded) {
        public BoardingTrip(String bus, String route, String departure, int expected, int capacity) {
            this(bus, route, departure, expected, capacity, 0);
        }
    }

    public record Station(String name, qpal.model.BookingData.QueueRow queue, String trip) {
        public Station(String name, qpal.model.BookingData.QueueRow queue) {
            this(name, queue, null);
        }
    }

    public static class Summary {
        public int available;
        public int maintenance;
        public int inactive;
        public int total;
        public int commuters;
        public String currentQueue = "— | —";
        public final List<String[]> departures = new ArrayList<>();
        public final List<BoardingTrip> boardingTrips = new ArrayList<>();
        public final List<Station> stations = new ArrayList<>();
    }

    public Summary loadSummary() throws SQLException {
        Summary summary = new Summary();
        java.util.Map<Integer, Integer> counters = new QueueDao().stations("Payment");
        QueueDao queueDao = new QueueDao();
        var gates = queueDao.stations("Boarding");
        var boarding = queueDao.boarding();
        var payments = queueDao.today();
        for (int number = 1; number <= 2; number++) {
            Integer id = counters.get(number);
            summary.stations.add(
                    new Station(
                            "Payment Counter " + number,
                            payments.stream()
                                    .filter(
                                            row ->
                                                    java.util.Objects.equals(id, row.id())
                                                            && "Serving".equals(row.status()))
                                    .findFirst()
                                    .orElse(null)));
        }
        for (int number = 1; number <= 2; number++) {
            Integer id = gates.get(number);
            summary.stations.add(
                    new Station(
                            "Boarding Gate " + number,
                            boarding.stream()
                                    .filter(row -> java.util.Objects.equals(id, row.id()))
                                    .findFirst()
                                    .orElse(null)));
        }
        try (Connection connection = DbConnection.getConnection()) {
            try (PreparedStatement p =
                            connection.prepareStatement(
                                    "SELECT g.gate,b.bus_number,r.origin,r.destination FROM"
                                        + " boarding_gates g JOIN trips t ON t.trip_id=g.trip_id"
                                        + " JOIN buses b ON b.bus_id=t.bus_id JOIN routes r ON"
                                        + " r.route_id=t.route_id");
                    ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    int index = r.getInt(1) + 1;
                    Station station = summary.stations.get(index);
                    summary.stations.set(
                            index,
                            new Station(
                                    station.name(),
                                    station.queue(),
                                    r.getString(2)
                                            + " • "
                                            + r.getString(3)
                                            + " → "
                                            + r.getString(4)));
                }
            }
            try (PreparedStatement statement =
                            connection.prepareStatement(
                                    "SELECT"
                                        + " b.bus_number,r.origin,r.destination,t.departure_date,t.departure_time,TIMESTAMP(t.departure_date,t.departure_time)<=NOW()"
                                        + " AS is_delayed,b.seat_capacity,COALESCE(counts.expected,0) AS expected,COALESCE(counts.boarded,0) AS boarded FROM"
                                        + " trips t JOIN buses b ON b.bus_id=t.bus_id JOIN routes r"
                                        + " ON r.route_id=t.route_id LEFT JOIN (SELECT bk.trip_id,COUNT(*) AS expected,SUM(CASE WHEN qb.queue_entry_id IS NOT NULL THEN 1 ELSE 0 END) AS boarded"
                                        + " FROM bookings bk JOIN booking_passengers bp ON bp.booking_id=bk.booking_id JOIN queue_entries q ON q.booking_id=bk.booking_id"
                                        + " LEFT JOIN queue_boarding qb ON qb.queue_entry_id=q.queue_entry_id WHERE bk.status NOT IN ('Cancelled','Expired','No-show')"
                                        + " AND q.status NOT IN ('Cancelled','Expired','No-show') GROUP BY bk.trip_id) counts ON counts.trip_id=t.trip_id WHERE t.status='Boarding' AND"
                                        + " EXISTS(SELECT 1 FROM boarding_gates g WHERE"
                                        + " g.trip_id=t.trip_id) ORDER BY"
                                        + " t.departure_date,t.departure_time");
                    ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    int capacity = rows.getInt("seat_capacity");
                    summary.boardingTrips.add(
                            new BoardingTrip(
                                    rows.getString("bus_number"),
                                    rows.getString("origin")
                                            + " → "
                                            + rows.getString("destination"),
                                    rows.getDate("departure_date").toLocalDate()
                                            + " • "
                                            + rows.getTime("departure_time")
                                                    .toLocalTime()
                                                    .format(
                                                            java.time.format.DateTimeFormatter
                                                                    .ofPattern(
                                                                            "hh:mm a",
                                                                            java.util.Locale
                                                                                    .ENGLISH))
                                            + (rows.getBoolean("is_delayed") ? " • Delayed" : ""),
                                    rows.getInt("expected"),
                                    capacity,
                                    rows.getInt("boarded")));
                }
            }
            try (PreparedStatement statement =
                            connection.prepareStatement(
                                    "SELECT COUNT(*) FROM booking_passengers bp JOIN queue_entries"
                                        + " q ON q.booking_id=bp.booking_id WHERE"
                                        + " q.queue_date=CURRENT_DATE AND q.status NOT IN"
                                        + " ('Cancelled','Expired','No-show')");
                    ResultSet rows = statement.executeQuery()) {
                rows.next();
                summary.commuters = rows.getInt(1);
            }
            try (PreparedStatement statement =
                            connection.prepareStatement(
                                    "SELECT queue_entry_id,queue_number FROM queue_entries WHERE"
                                        + " queue_date=CURRENT_DATE AND status='Serving'");
                    ResultSet rows = statement.executeQuery()) {
                String[] queues = {"—", "—"};
                while (rows.next()) {
                    for (int counter = 1; counter <= 2; counter++) {
                        if (Integer.valueOf(rows.getInt(1)).equals(counters.get(counter))) {
                            queues[counter - 1] =
                                    String.format(java.util.Locale.ROOT, "P%03d", rows.getInt(2));
                        }
                    }
                }
                summary.currentQueue = queues[0] + " | " + queues[1];
            }
            try (PreparedStatement statement =
                            connection.prepareStatement(
                                    "SELECT bus_status, COUNT(*) AS total FROM buses GROUP BY"
                                        + " bus_status");
                    ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    int count = rows.getInt("total");
                    summary.total += count;
                    String status = rows.getString("bus_status");
                    if ("Available".equalsIgnoreCase(status)) {

                        summary.available += count;
                    }
                    if ("Maintenance".equalsIgnoreCase(status)) {

                        summary.maintenance += count;
                    }
                    if ("Inactive".equalsIgnoreCase(status)) {

                        summary.inactive += count;
                    }
                }
            }
            try (PreparedStatement statement =
                            connection.prepareStatement(
                                    "SELECT b.bus_number, r.origin, r.destination,"
                                        + " t.departure_date, t.departure_time FROM trips t JOIN"
                                        + " buses b ON b.bus_id = t.bus_id JOIN routes r ON"
                                        + " r.route_id = t.route_id WHERE t.status IN ('Scheduled',"
                                        + " 'Boarding') AND TIMESTAMP(t.departure_date,"
                                        + " t.departure_time) >= NOW() ORDER BY t.departure_date,"
                                        + " t.departure_time LIMIT 3");
                    ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    summary.departures.add(
                            new String[] {
                                rows.getString("origin") + " → " + rows.getString("destination"),
                                rows.getString("bus_number")
                                        + " • "
                                        + rows.getString("departure_date")
                                        + "  "
                                        + rows.getString("departure_time")
                            });
                }
            }
        }
        return summary;
    }
}
