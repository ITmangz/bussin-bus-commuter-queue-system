package qpal.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import qpal.model.BookingData.*;
import qpal.util.DbConnection;

public class BookingDao {
    private static void ensureDropPoints(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS booking_dropoffs (booking_id INT NOT NULL PRIMARY"
                        + " KEY, drop_off VARCHAR(150) NOT NULL)");
        }
        try (Statement s = c.createStatement()) {
            s.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS booking_fare_details (booking_passenger_id INT NOT"
                        + " NULL PRIMARY KEY, base_fare DECIMAL(12,2) NOT NULL, FOREIGN KEY"
                        + " (booking_passenger_id) REFERENCES"
                        + " booking_passengers(booking_passenger_id) ON DELETE CASCADE)"
                        + " ENGINE=InnoDB");
        }
    }

    private static final String TRIP_SELECT =
            "SELECT t.*, b.bus_number, b.seat_capacity, r.origin, r.destination, r.fare FROM trips"
                + " t JOIN buses b ON b.bus_id=t.bus_id JOIN routes r ON r.route_id=t.route_id ";
    private static final String BOOKABLE =
            "t.status IN ('Scheduled','Boarding') AND r.status='Active' AND"
                + " b.bus_status='Available' AND TIMESTAMP(t.departure_date,t.departure_time)>NOW()"
                + " ";

    private TripOption trip(ResultSet r) throws SQLException {
        return new TripOption(
                r.getInt("trip_id"),
                r.getString("bus_number"),
                r.getString("origin"),
                r.getString("destination"),
                r.getDate("departure_date").toLocalDate(),
                r.getTime("departure_time").toLocalTime(),
                r.getBigDecimal("fare"),
                r.getInt("seat_capacity"),
                r.getInt("available_seats"));
    }

    public List<TripOption> availableTrips() throws SQLException {
        try (Connection c = DbConnection.getConnection();
                PreparedStatement p =
                        c.prepareStatement(
                                TRIP_SELECT
                                        + "WHERE "
                                        + BOOKABLE
                                        + "AND t.available_seats>0 ORDER BY"
                                        + " t.departure_date,t.departure_time,t.trip_id");
                ResultSet r = p.executeQuery()) {
            List<TripOption> result = new ArrayList<>();
            while (r.next()) result.add(trip(r));
            return result;
        }
    }

    public Set<Integer> occupiedSeats(int tripId) throws SQLException {
        try (Connection c = DbConnection.getConnection();
                PreparedStatement p =
                        statement(
                                c,
                                "SELECT seat_number FROM seat_reservations WHERE trip_id=? AND"
                                    + " status='Active'",
                                tripId);
                ResultSet r = p.executeQuery()) {
            Set<Integer> result = new HashSet<>();
            while (r.next()) result.add(r.getInt(1));
            return result;
        }
    }

    public static String seatLabel(int seat) {
        return ((seat - 1) / 4 + 1) + "" + (char) ('A' + (seat - 1) % 4);
    }

    public static int seatNumber(String label) {
        return (Integer.parseInt(label.substring(0, label.length() - 1)) - 1) * 4
                + label.charAt(label.length() - 1)
                - 'A'
                + 1;
    }

    public Receipt book(
            String reference, TripOption selected, List<Passenger> passengers, String method)
            throws SQLException {
        return book(
                reference,
                selected,
                passengers,
                method,
                selected == null ? null : selected.destination());
    }

    public Receipt book(
            String reference,
            TripOption selected,
            List<Passenger> passengers,
            String method,
            String dropPoint)
            throws SQLException {
        if (selected == null
                || passengers.isEmpty()
                || passengers.size() > 10
                || !Set.of("Cash", "GCash", "Card").contains(method))
            throw new SQLException("Please complete your booking details.");
        Set<Integer> seats = new HashSet<>();
        for (Passenger p : passengers) {
            if (p.name() == null
                    || p.name().trim().isEmpty()
                    || p.name().length() > 100
                    || !Set.of("Regular", "Student", "Senior", "PWD").contains(p.type())
                    || !seats.add(p.seat()))
                throw new SQLException("Enter a name and a different seat for each passenger.");
        }
        try (Connection c = DbConnection.getConnection()) {
            ensureDropPoints(c);
            c.setAutoCommit(false);
            try {
                // Serializes bookings for this trip, including repeat submits of the same
                // reference.
                TripOption live;
                try (PreparedStatement p =
                                statement(
                                        c,
                                        TRIP_SELECT + "WHERE t.trip_id=? FOR UPDATE",
                                        selected.id());
                        ResultSet r = p.executeQuery()) {
                    if (!r.next())
                        throw new SQLException(
                                "This trip no longer exists. Please select another trip.");
                    live = trip(r);
                }
                try (PreparedStatement p =
                                statement(
                                        c,
                                        "SELECT booking_id FROM bookings WHERE booking_reference=?",
                                        reference);
                        ResultSet r = p.executeQuery()) {
                    if (r.next()) {
                        Receipt receipt = receipt(c, r.getInt(1));
                        c.commit();
                        return receipt;
                    }
                }
                try (PreparedStatement p =
                                statement(
                                        c,
                                        TRIP_SELECT + "WHERE t.trip_id=? AND " + BOOKABLE,
                                        selected.id());
                        ResultSet r = p.executeQuery()) {
                    if (!r.next())
                        throw new SQLException(
                                "This trip is no longer available. Please select another trip.");
                }
                if (!live.date().equals(selected.date())
                        || !live.time().equals(selected.time())
                        || !live.bus().equals(selected.bus())
                        || !live.route().equals(selected.route())
                        || live.fare().compareTo(selected.fare()) != 0)
                    throw new SQLException(
                            "The trip details or fare changed. Please select the trip again.");
                if (live.available() < passengers.size())
                    throw new SQLException("Not enough seats remain. Please select another trip.");
                for (Passenger p : passengers)
                    if (p.seat() < 1 || p.seat() > live.capacity())
                        throw new SQLException(
                                "The bus seat layout changed. Please select your seats again.");
                if (!qpal.model.RouteDropPoints.valid(live.origin(), live.destination(), dropPoint))
                    throw new SQLException("Please select a valid drop-off for this route.");
                BigDecimal passengerFare =
                        qpal.model.RouteDropPoints.fare(live.destination(), dropPoint, live.fare());
                BigDecimal total =
                        qpal.model.FarePolicy.total(
                                passengerFare, passengers.stream().map(Passenger::type).toList());
                int booking =
                        insert(
                                c,
                                "INSERT INTO bookings"
                                    + " (booking_reference,trip_id,total_amount,status) VALUES"
                                    + " (?,?,?,'Pending')",
                                reference,
                                live.id(),
                                total);
                update(
                        c,
                        "INSERT INTO booking_dropoffs (booking_id,drop_off) VALUES (?,?)",
                        booking,
                        dropPoint);
                for (Passenger passenger : passengers) {
                    int person =
                            insert(
                                    c,
                                    "INSERT INTO booking_passengers"
                                        + " (booking_id,trip_id,passenger_name,passenger_type,fare)"
                                        + " VALUES (?,?,?,?,?)",
                                    booking,
                                    live.id(),
                                    passenger.name().trim(),
                                    passenger.type(),
                                    qpal.model.FarePolicy.passengerFare(
                                            passengerFare, passenger.type()));
                    update(
                            c,
                            "INSERT INTO booking_fare_details (booking_passenger_id,base_fare)"
                                + " VALUES (?,?)",
                            person,
                            passengerFare);
                    update(
                            c,
                            "INSERT INTO seat_reservations"
                                + " (trip_id,booking_passenger_id,seat_number,status) VALUES"
                                + " (?,?,?,'Active')",
                            live.id(),
                            person,
                            passenger.seat());
                }
                update(
                        c,
                        "UPDATE trips SET available_seats=available_seats-? WHERE trip_id=?",
                        passengers.size(),
                        live.id());
                update(
                        c,
                        "INSERT INTO payments"
                            + " (trip_id,booking_id,commuter_name,amount,payment_method,status)"
                            + " VALUES (?,?,?,?,?,'Pending')",
                        live.id(),
                        booking,
                        passengers.get(0).name().trim(),
                        total,
                        method);
                LocalDate day;
                try (Statement p = c.createStatement();
                        ResultSet r = p.executeQuery("SELECT CURRENT_DATE")) {
                    r.next();
                    day = r.getDate(1).toLocalDate();
                }
                update(
                        c,
                        "INSERT INTO queue_daily_counters (queue_date,last_queue_number) VALUES"
                            + " (?,0) ON DUPLICATE KEY UPDATE last_queue_number=last_queue_number",
                        day);
                // Include existing queue entries if the counter was introduced after them.
                update(
                        c,
                        "UPDATE queue_daily_counters SET"
                            + " last_queue_number=GREATEST(last_queue_number,(SELECT"
                            + " COALESCE(MAX(queue_number),0) FROM queue_entries WHERE"
                            + " queue_date=?))+1 WHERE queue_date=?",
                        day,
                        day);
                int number;
                try (PreparedStatement p =
                                statement(
                                        c,
                                        "SELECT last_queue_number FROM queue_daily_counters WHERE"
                                            + " queue_date=?",
                                        day);
                        ResultSet r = p.executeQuery()) {
                    r.next();
                    number = r.getInt(1);
                }
                update(
                        c,
                        "INSERT INTO queue_entries (booking_id,queue_date,queue_number,status)"
                            + " VALUES (?,?,?,'Waiting')",
                        booking,
                        day,
                        number);
                Receipt receipt = receipt(c, booking);
                c.commit();
                ActivityLogDao.recordActivity(
                        "Queue Management",
                        "Create",
                        "Created booking #" + booking + " with queue number " + number + ".");
                return receipt;
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                if (ex instanceof SQLException && ((SQLException) ex).getErrorCode() == 1062)
                    throw new SQLException(
                            "A selected seat was just booked. Go back and choose available seats.",
                            ex);
                throw ex;
            }
        }
    }

    public Receipt receipt(int booking) throws SQLException {
        try (Connection c = DbConnection.getConnection()) {
            ensureDropPoints(c);
            return receipt(c, booking);
        }
    }

    private Receipt receipt(Connection c, int booking) throws SQLException {
        List<FareLine> fares = new ArrayList<>();
        try (PreparedStatement p =
                        statement(
                                c,
                                "SELECT bp.passenger_type,fd.base_fare,bp.fare FROM"
                                    + " booking_passengers bp LEFT JOIN booking_fare_details fd ON"
                                    + " fd.booking_passenger_id=bp.booking_passenger_id WHERE"
                                    + " bp.booking_id=? ORDER BY bp.booking_passenger_id",
                                booking);
                ResultSet r = p.executeQuery()) {
            while (r.next())
                fares.add(new FareLine(r.getString(1), r.getBigDecimal(2), r.getBigDecimal(3)));
        }
        String sql =
                "SELECT"
                    + " bk.booking_reference,bk.total_amount,q.queue_date,q.queue_number,b.bus_number,r.origin,COALESCE(d.drop_off,r.destination),t.departure_date,t.departure_time,p.payment_method,p.status"
                    + " FROM bookings bk LEFT JOIN booking_dropoffs d ON d.booking_id=bk.booking_id"
                    + " JOIN trips t ON t.trip_id=bk.trip_id JOIN buses b ON b.bus_id=t.bus_id JOIN"
                    + " routes r ON r.route_id=t.route_id JOIN queue_entries q ON"
                    + " q.booking_id=bk.booking_id JOIN payments p ON p.booking_id=bk.booking_id"
                    + " WHERE bk.booking_id=? ORDER BY p.payment_id DESC LIMIT 1";
        List<String> labels = new ArrayList<>();
        try (PreparedStatement p =
                        statement(
                                c,
                                "SELECT s.seat_number FROM seat_reservations s JOIN"
                                    + " booking_passengers bp ON"
                                    + " bp.booking_passenger_id=s.booking_passenger_id WHERE"
                                    + " bp.booking_id=? AND s.status='Active' ORDER BY"
                                    + " bp.booking_passenger_id",
                                booking);
                ResultSet r = p.executeQuery()) {
            while (r.next()) labels.add(seatLabel(r.getInt(1)));
        }
        try (PreparedStatement p = statement(c, sql, booking);
                ResultSet r = p.executeQuery()) {
            if (!r.next()) throw new SQLException("Booking ticket is unavailable.");
            return new Receipt(
                    booking,
                    r.getString(1),
                    r.getDate(3).toLocalDate(),
                    r.getInt(4),
                    r.getString(5),
                    r.getString(6) + " - " + r.getString(7),
                    r.getString(8) + " | " + r.getString(9),
                    String.join(", ", labels),
                    r.getBigDecimal(2),
                    r.getString(10),
                    r.getString(11),
                    fares);
        }
    }

    static PreparedStatement statement(Connection c, String sql, Object... args)
            throws SQLException {
        PreparedStatement p = c.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) p.setObject(i + 1, args[i]);
        return p;
    }

    static int update(Connection c, String sql, Object... args) throws SQLException {
        try (PreparedStatement p = statement(c, sql, args)) {
            return p.executeUpdate();
        }
    }

    private static int insert(Connection c, String sql, Object... args) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < args.length; i++) p.setObject(i + 1, args[i]);
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (!r.next()) throw new SQLException("No booking ID returned.");
                return r.getInt(1);
            }
        }
    }
}
