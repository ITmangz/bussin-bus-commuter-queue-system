package qpal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import qpal.model.Trip;
import qpal.util.DbConnection;

public class TripDao {
    public static class DepartedTripException extends IllegalArgumentException {
        public DepartedTripException() {
            super("Trip is departed.");
        }
    }

    public static class ScheduleConflictException extends IllegalArgumentException {
        public ScheduleConflictException(String message) {
            super(message);
        }
    }

    private void checkBusSchedule(Connection conn, Trip trip, boolean editing) throws SQLException {
        // Serialize schedule saves for this bus, including simultaneous additions.
        try (PreparedStatement lock =
                conn.prepareStatement("SELECT bus_id FROM buses WHERE bus_id=? FOR UPDATE")) {
            lock.setInt(1, trip.getBusID());
            try (ResultSet result = lock.executeQuery()) {
                if (!result.next()) throw new SQLException("Bus no longer exists.");
            }
        }
        if ("Cancelled".equalsIgnoreCase(trip.getStatus())) return;
        java.time.LocalDateTime departure =
                java.time.LocalDate.parse(trip.getDepartureDate())
                        .atTime(java.time.LocalTime.parse(trip.getDepartureTime()));
        String sql =
                "SELECT departure_date, departure_time FROM trips WHERE bus_id=? "
                        + "AND trip_id<>? AND status<>'Cancelled' "
                        + "AND TIMESTAMP(departure_date,departure_time)>? "
                        + "AND TIMESTAMP(departure_date,departure_time)<? "
                        + "ORDER BY departure_date,departure_time LIMIT 1 FOR UPDATE";
        try (PreparedStatement check = conn.prepareStatement(sql)) {
            check.setInt(1, trip.getBusID());
            check.setInt(2, editing ? trip.getTripID() : 0);
            check.setTimestamp(3, java.sql.Timestamp.valueOf(departure.minusHours(6)));
            check.setTimestamp(4, java.sql.Timestamp.valueOf(departure.plusHours(6)));
            try (ResultSet result = check.executeQuery()) {
                if (result.next()) {
                    java.time.LocalDateTime existing =
                            result.getDate("departure_date")
                                    .toLocalDate()
                                    .atTime(result.getTime("departure_time").toLocalTime());
                    String when =
                            existing.format(
                                    java.time.format.DateTimeFormatter.ofPattern(
                                            "MMM d, yyyy 'at' h:mm a", java.util.Locale.ENGLISH));
                    throw new ScheduleConflictException(
                            "This bus already has a departure on "
                                    + when
                                    + ".\n"
                                    + "Allow at least 6 hours between departures for travel and"
                                    + " vacant time.\n"
                                    + "Please choose another departure date/time or bus.");
                }
            }
        }
    }

    public List<Trip> getAllTrips() {

        List<Trip> trips = new ArrayList<>();

        String sql =
                "SELECT t.trip_id, "
                        + "t.bus_id, "
                        + "t.route_id, "
                        + "b.bus_number, "
                        + "r.origin, "
                        + "r.destination, "
                        + "r.fare, "
                        + "t.departure_date, "
                        + "t.departure_time, "
                        + "t.available_seats, "
                        + "b.seat_capacity, "
                        + "t.status "
                        + "FROM trips t "
                        + "INNER JOIN buses b ON t.bus_id = b.bus_id "
                        + "INNER JOIN routes r ON t.route_id = r.route_id "
                        + "ORDER BY t.trip_id ASC";

        try (Connection conn = DbConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {

                Trip trip = new Trip();

                trip.setTripID(rs.getInt("trip_id"));
                trip.setBusID(rs.getInt("bus_id"));
                trip.setRouteID(rs.getInt("route_id"));

                trip.setBusName(rs.getString("bus_number"));
                trip.setOrigin(rs.getString("origin"));
                trip.setDestination(rs.getString("destination"));

                trip.setFare(rs.getDouble("fare"));

                trip.setDepartureDate(rs.getString("departure_date"));
                trip.setDepartureTime(rs.getString("departure_time"));

                trip.setAvailableSeats(rs.getInt("available_seats"));
                trip.setSeatCapacity(rs.getInt("seat_capacity"));

                trip.setStatus(rs.getString("status"));

                trips.add(trip);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return trips;
    }

    public Trip getTrip(int id) {

        String sql =
                "SELECT t.trip_id, "
                        + "t.bus_id, "
                        + "t.route_id, "
                        + "b.bus_number, "
                        + "r.origin, "
                        + "r.destination, "
                        + "r.fare, "
                        + "t.departure_date, "
                        + "t.departure_time, "
                        + "t.available_seats, "
                        + "t.status "
                        + "FROM trips t "
                        + "INNER JOIN buses b ON t.bus_id = b.bus_id "
                        + "INNER JOIN routes r ON t.route_id = r.route_id "
                        + "WHERE t.trip_id = ?";

        try (Connection conn = DbConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, id);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {

                Trip trip = new Trip();

                trip.setTripID(rs.getInt("trip_id"));
                trip.setBusID(rs.getInt("bus_id"));
                trip.setRouteID(rs.getInt("route_id"));

                trip.setBusName(rs.getString("bus_number"));
                trip.setOrigin(rs.getString("origin"));
                trip.setDestination(rs.getString("destination"));

                trip.setFare(rs.getDouble("fare"));

                trip.setDepartureDate(rs.getString("departure_date"));
                trip.setDepartureTime(rs.getString("departure_time"));

                trip.setAvailableSeats(rs.getInt("available_seats"));

                trip.setStatus(rs.getString("status"));

                return trip;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean addTrip(Trip trip) {

        String sql =
                "INSERT INTO trips (bus_id, route_id, departure_date, departure_time,"
                    + " available_seats, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DbConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            try {
                checkBusSchedule(conn, trip, false);
                pst.setInt(1, trip.getBusID());
                pst.setInt(2, trip.getRouteID());
                pst.setString(3, trip.getDepartureDate());
                pst.setString(4, trip.getDepartureTime());
                pst.setInt(5, trip.getAvailableSeats());
                pst.setString(6, trip.getStatus());

                boolean saved = pst.executeUpdate() > 0;
                conn.commit();
                if (saved)
                    ActivityLogDao.recordActivity(
                            "Route & Schedule",
                            "Create",
                            "Added trip for bus #"
                                    + trip.getBusID()
                                    + " on "
                                    + trip.getDepartureDate()
                                    + " at "
                                    + trip.getDepartureTime()
                                    + ".");
                return saved;
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateTrip(Trip trip) {
        try (Connection conn = DbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                checkBusSchedule(conn, trip, true);
                protectBookedTrip(conn, trip);
                try (PreparedStatement p =
                        conn.prepareStatement(
                                "UPDATE trips SET"
                                    + " bus_id=?,route_id=?,departure_date=?,departure_time=?,available_seats=?,status=?"
                                    + " WHERE trip_id=?")) {
                    p.setInt(1, trip.getBusID());
                    p.setInt(2, trip.getRouteID());
                    p.setString(3, trip.getDepartureDate());
                    p.setString(4, trip.getDepartureTime());
                    p.setInt(5, trip.getAvailableSeats());
                    p.setString(6, trip.getStatus());
                    p.setInt(7, trip.getTripID());
                    boolean updated = p.executeUpdate() > 0;
                    conn.commit();
                    if (updated)
                        ActivityLogDao.recordActivity(
                                "Route & Schedule",
                                "Update",
                                "Updated trip #"
                                        + trip.getTripID()
                                        + ", status: "
                                        + trip.getStatus()
                                        + ".");
                    return updated;
                }
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean deleteTrip(int id) {
        try {
            return new DeleteDao().delete(DeleteDao.Target.TRIP, id);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean saveTripWithFare(Trip trip, java.math.BigDecimal fare, boolean editing) {
        if (!qpal.model.FarePolicy.validRouteFare(fare))
            throw new IllegalArgumentException("Enter a positive whole-peso fare (no decimals).");

        try (Connection conn = DbConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                String sql;
                checkBusSchedule(conn, trip, editing);

                if (editing) {

                    protectBookedTrip(conn, trip);

                    sql =
                            "UPDATE trips SET bus_id=?, route_id=?, departure_date=?,"
                                + " departure_time=?, available_seats=?, status=? WHERE trip_id=?";

                } else {

                    sql =
                            "INSERT INTO trips"
                                + " (bus_id,route_id,departure_date,departure_time,available_seats,status)"
                                + " VALUES (?,?,?,?,?,?)";
                }

                try (PreparedStatement pst = conn.prepareStatement(sql)) {

                    pst.setInt(1, trip.getBusID());
                    pst.setInt(2, trip.getRouteID());
                    pst.setString(3, trip.getDepartureDate());
                    pst.setString(4, trip.getDepartureTime());
                    pst.setInt(5, trip.getAvailableSeats());
                    pst.setString(6, trip.getStatus());

                    if (editing) {

                        pst.setInt(7, trip.getTripID());
                    }

                    if (pst.executeUpdate() == 0) {

                        conn.rollback();
                        return false;
                    }
                }

                try (PreparedStatement pst =
                        conn.prepareStatement("UPDATE routes SET fare=? WHERE route_id=?")) {

                    pst.setBigDecimal(1, fare);
                    pst.setInt(2, trip.getRouteID());
                    pst.executeUpdate();
                }

                conn.commit();
                ActivityLogDao.recordActivity(
                        "Route & Schedule",
                        editing ? "Update" : "Create",
                        (editing ? "Updated" : "Added")
                                + " trip for bus #"
                                + trip.getBusID()
                                + " on "
                                + trip.getDepartureDate()
                                + " at "
                                + trip.getDepartureTime()
                                + ", fare: "
                                + fare
                                + ".");
                return true;

            } catch (ScheduleConflictException | DepartedTripException e) {
                conn.rollback();
                throw e;
            } catch (SQLException e) {

                conn.rollback();
                e.printStackTrace();
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return false;
    }

    private void protectBookedTrip(Connection conn, Trip trip) throws SQLException {
        try (PreparedStatement p =
                conn.prepareStatement("SELECT * FROM trips WHERE trip_id=? FOR UPDATE")) {
            p.setInt(1, trip.getTripID());
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new SQLException("Trip no longer exists.");
                if ("Departed".equals(r.getString("status"))
                        && !"Departed".equals(trip.getStatus())) throw new DepartedTripException();
                try (PreparedStatement bookings =
                        conn.prepareStatement(
                                "SELECT booking_id FROM bookings WHERE trip_id=? LIMIT 1")) {
                    bookings.setInt(1, trip.getTripID());
                    try (ResultSet found = bookings.executeQuery()) {
                        if (!found.next()) return;
                    }
                }
                if (r.getInt("bus_id") != trip.getBusID()
                        || r.getInt("route_id") != trip.getRouteID()
                        || !r.getDate("departure_date")
                                .toLocalDate()
                                .equals(java.time.LocalDate.parse(trip.getDepartureDate()))
                        || !r.getTime("departure_time")
                                .toLocalTime()
                                .equals(java.time.LocalTime.parse(trip.getDepartureTime()))
                        || "Cancelled".equals(trip.getStatus()))
                    throw new SQLException(
                            "A booked trip cannot change bus, route, departure, or be cancelled"
                                + " here.");
                trip.setAvailableSeats(r.getInt("available_seats"));
            }
        }
    }
}
