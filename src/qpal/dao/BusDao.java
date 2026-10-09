package qpal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import qpal.model.Bus;
import qpal.util.DbConnection;

public class BusDao {
    /** Prefer boarding, then the next scheduled trip, then the latest trip history. */
    public java.util.Map<Integer,String> departureStatuses() throws SQLException {
        var statuses = new java.util.HashMap<Integer,String>();
        try (Connection c = DbConnection.getConnection()) {
            qpal.util.DepartureService.reconcile(c);
            try (PreparedStatement p = c.prepareStatement(
                    "SELECT bus_id,status FROM trips WHERE status='Boarding' "
                    + "OR status IN ('Scheduled','Cancelled','Departed') "
                    + "ORDER BY CASE status WHEN 'Boarding' THEN 0 WHEN 'Scheduled' THEN 1 ELSE 2 END, "
                    + "CASE WHEN status IN ('Boarding','Scheduled') THEN TIMESTAMP(departure_date,departure_time) END ASC, "
                    + "TIMESTAMP(departure_date,departure_time) DESC,trip_id DESC"); ResultSet r = p.executeQuery()) {
                while (r.next()) statuses.putIfAbsent(r.getInt(1),r.getString(2));
            }
        }
        return statuses;
    }


    public List<Bus> getAllBuses() {

        List<Bus> buses = new ArrayList<>();

        String sql = "SELECT * FROM buses ORDER BY bus_id ASC";

        try (
                Connection conn = DbConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {

                Bus bus = new Bus();

                bus.setBusID(rs.getInt("bus_id"));
                bus.setBusNumber(rs.getString("bus_number"));
                bus.setSeatCapacity(rs.getInt("seat_capacity"));
                bus.setAvailableSeats(rs.getInt("available_seats"));
                bus.setBusStatus(rs.getString("bus_status"));

                buses.add(bus);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return buses;
    }

    public Bus getBus(int id) {

        String sql = "SELECT * FROM buses WHERE bus_id = ?";

        try (
                Connection conn = DbConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, id);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {

                Bus bus = new Bus();

                bus.setBusID(rs.getInt("bus_id"));
                bus.setBusNumber(rs.getString("bus_number"));
                bus.setSeatCapacity(rs.getInt("seat_capacity"));
                bus.setAvailableSeats(rs.getInt("available_seats"));
                bus.setBusStatus(rs.getString("bus_status"));

                return bus;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean addBus(Bus bus) {

        String sql = "INSERT INTO buses (bus_number, seat_capacity, available_seats, bus_status) VALUES (?, ?, ?, ?)";

        try (Connection con = DbConnection.getConnection();
            PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, bus.getBusNumber());
            pst.setInt(2, bus.getSeatCapacity());
            pst.setInt(3, bus.getAvailableSeats());
            pst.setString(4, bus.getBusStatus());

            boolean saved = pst.executeUpdate() > 0;
            if (saved) ActivityLogDao.recordActivity("Bus Management", "Create", "Added bus " + bus.getBusNumber() + " with " + bus.getSeatCapacity() + " seats.");
            return saved;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateBus(Bus bus) {
        try { return updateBusDetails(bus); }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    public boolean updateBusDetails(Bus bus) throws SQLException {

        String sql = "UPDATE buses SET bus_number=?, seat_capacity=?, available_seats=?, bus_status=? WHERE bus_id=? "
                + "AND (seat_capacity=? OR NOT EXISTS (SELECT 1 FROM trips t JOIN bookings bk ON bk.trip_id=t.trip_id "
                + "WHERE t.bus_id=buses.bus_id AND t.status IN ('Scheduled','Boarding')))";

        try (Connection con = DbConnection.getConnection();
            PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, bus.getBusNumber());
            pst.setInt(2, bus.getSeatCapacity());
            pst.setInt(3, bus.getAvailableSeats());
            pst.setString(4, bus.getBusStatus());
            pst.setInt(5, bus.getBusID());
            pst.setInt(6, bus.getSeatCapacity());

            con.setAutoCommit(false);
            try {
                boolean saved = pst.executeUpdate() > 0;
                if (!saved) throw new SQLException("This bus was removed, or a scheduled/boarding trip has bookings. Keep its current seat capacity and try again.");
                // Unbooked active trips must use the updated bus capacity too.
                try (PreparedStatement trips = con.prepareStatement("UPDATE trips t SET available_seats=? WHERE bus_id=? "
                        + "AND status IN ('Scheduled','Boarding') AND NOT EXISTS (SELECT 1 FROM bookings bk WHERE bk.trip_id=t.trip_id)")) {
                    trips.setInt(1, bus.getSeatCapacity());
                    trips.setInt(2, bus.getBusID());
                    trips.executeUpdate();
                }
                con.commit();
                ActivityLogDao.recordActivity("Bus Management", "Update", "Updated bus #" + bus.getBusID() + " (" + bus.getBusNumber() + "), status: " + bus.getBusStatus() + ".");
                return true;
            } catch (SQLException | RuntimeException ex) { con.rollback(); throw ex; }
        }
    }

    public boolean deleteBus(int id) {
        try { return new DeleteDao().delete(DeleteDao.Target.BUS, id); }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public boolean checkBusNumber(String busNumber) {

        String sql = "SELECT * FROM buses WHERE bus_number=?";

        try (
                Connection conn = DbConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, busNumber);

            ResultSet rs = pst.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    

}
