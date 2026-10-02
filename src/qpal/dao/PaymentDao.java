package qpal.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import static qpal.dao.BookingDao.*;
import qpal.util.DbConnection;

public class PaymentDao {
    public record Payment(int id, int tripId, Integer bookingId, String commuter, BigDecimal amount,
            String method, String status, Timestamp paidAt) {}
    public record Choice(int tripId, Integer bookingId, String label, BigDecimal amount) {
        @Override public String toString() { return label; }
    }
    public List<Choice> choices() throws SQLException {
        List<Choice> choices = new ArrayList<>();
        try (Connection c = DbConnection.getConnection()) {
            try (PreparedStatement p = c.prepareStatement("SELECT t.trip_id,b.bus_number,r.origin,r.destination,t.departure_date,t.departure_time,r.fare "
                    + "FROM trips t JOIN buses b ON b.bus_id=t.bus_id JOIN routes r ON r.route_id=t.route_id ORDER BY t.departure_date DESC,t.trip_id DESC");
                    ResultSet r = p.executeQuery()) {
                while(r.next()) choices.add(new Choice(r.getInt(1),null,"Trip #" + r.getInt(1) + " | " + r.getString(2) + " | "
                        + r.getString(3) + " - " + r.getString(4) + " | " + r.getString(5) + " " + r.getString(6),r.getBigDecimal(7)));
            }
            try (PreparedStatement p = c.prepareStatement("SELECT bk.booking_id,bk.trip_id,bk.booking_reference,bk.total_amount FROM bookings bk "
                    + "WHERE bk.status<>'Cancelled' AND NOT EXISTS(SELECT 1 FROM payments p WHERE p.booking_id=bk.booking_id) ORDER BY bk.booking_id DESC");
                    ResultSet r = p.executeQuery()) {
                while(r.next()) choices.add(0,new Choice(r.getInt(2),r.getInt(1),"Booking " + r.getString(3) + " | Trip #" + r.getInt(2),r.getBigDecimal(4)));
            }
        }
        return choices;
    }
    public Payment get(int id) throws SQLException {
        try (Connection c = DbConnection.getConnection()) { return get(c,id,false); }
    }
    private Payment get(Connection c,int id,boolean lock) throws SQLException {
        try (PreparedStatement p = statement(c,"SELECT * FROM payments WHERE payment_id=?" + (lock ? " FOR UPDATE" : ""),id);
                ResultSet r = p.executeQuery()) {
            if (!r.next()) throw new SQLException("This payment no longer exists. Refresh the list.");
            int booking = r.getInt("booking_id"); Integer bookingId = r.wasNull() ? null : booking;
            return new Payment(id,r.getInt("trip_id"),bookingId,r.getString("commuter_name"),r.getBigDecimal("amount"),
                    r.getString("payment_method"),r.getString("status"),r.getTimestamp("paid_at"));
        }
    }
    public void save(Payment original,Choice choice,String commuter,BigDecimal amount,String method,String status) throws SQLException {
        if (choice == null || commuter == null || commuter.trim().isEmpty() || commuter.trim().length()>100
                || amount == null || amount.signum()<0 || amount.compareTo(new BigDecimal("99999999.99"))>0
                || amount.stripTrailingZeros().scale()>2 || method==null || !Set.of("Cash","GCash","Card").contains(method)
                || status==null || !Set.of("Pending","Paid","Cancelled").contains(status))
            throw new SQLException("Choose a trip, enter a label (1–100 characters), and a valid amount with up to two decimal places.");
        try (Connection c = DbConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                if (original != null) {
                    if (!original.equals(get(c,original.id(),true))) throw new SQLException("This payment changed. Close and reopen the dialog.");
                    if (!Objects.equals(original.bookingId(),choice.bookingId()) || original.tripId()!=choice.tripId())
                        throw new SQLException("An existing payment must keep its trip and booking.");
                }
                if (choice.bookingId()!=null) {
                    try (PreparedStatement p = statement(c,"SELECT trip_id,total_amount,status FROM bookings WHERE booking_id=? FOR UPDATE",choice.bookingId());
                            ResultSet r = p.executeQuery()) {
                        if (!r.next() || r.getInt(1)!=choice.tripId()) throw new SQLException("Booking and trip do not match.");
                        if (r.getBigDecimal(2).compareTo(amount)!=0) throw new SQLException("A booking payment must match the booking total.");
                        if (r.getString(3).equals("Cancelled")) throw new SQLException("This booking is cancelled.");
                    }
                    if (original==null) try (PreparedStatement p = statement(c,"SELECT payment_id FROM payments WHERE booking_id=?",choice.bookingId());
                            ResultSet r = p.executeQuery()) {
                        if (r.next()) throw new SQLException("This booking already has a payment. Edit that record instead.");
                    }
                }
                if (original==null) update(c,"INSERT INTO payments(trip_id,booking_id,commuter_name,amount,payment_method,status,paid_at) "
                        + "VALUES(?,?,?,?,?,?,CASE WHEN ?='Paid' THEN NOW() ELSE NULL END)",choice.tripId(),choice.bookingId(),commuter.trim(),amount,method,status,status);
                else update(c,"UPDATE payments SET commuter_name=?,amount=?,payment_method=?,paid_at=CASE WHEN ?='Paid' "
                        + "THEN COALESCE(paid_at,NOW()) ELSE NULL END,status=? WHERE payment_id=?",commuter.trim(),amount,method,status,status,original.id());
                syncBooking(c,choice.bookingId());
                c.commit();
                ActivityLogDao.recordActivity("Revenue", original == null ? "Create" : "Update", "Saved payment for trip #" + choice.tripId() + ", amount: " + amount + ", status: " + status + ".");
            } catch(SQLException | RuntimeException ex) { c.rollback(); throw ex; }
        }
    }
    public void delete(Payment original) throws SQLException {
        try (Connection c = DbConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                if (!original.equals(get(c,original.id(),true))) throw new SQLException("This payment changed. Refresh and try again.");
                update(c,"DELETE FROM payments WHERE payment_id=?",original.id());
                syncBooking(c,original.bookingId());
                c.commit();
                ActivityLogDao.recordActivity("Revenue", "Delete", "Deleted payment #" + original.id() + ".");
            } catch(SQLException | RuntimeException ex) { c.rollback(); throw ex; }
        }
    }
    private void syncBooking(Connection c,Integer booking) throws SQLException {
        if (booking==null) return;
        boolean paid;
        try (PreparedStatement p = statement(c,"SELECT status FROM payments WHERE booking_id=? ORDER BY payment_id DESC LIMIT 1",booking);
                ResultSet r = p.executeQuery()) { paid = r.next() && r.getString(1).equals("Paid"); }
        if (!paid) {
            update(c,"UPDATE bookings SET status='Pending' WHERE booking_id=? AND status<>'Cancelled'",booking);
            update(c,"UPDATE queue_entries SET status='Waiting',completed_at=NULL WHERE booking_id=? AND status='Completed'",booking);
        } else update(c,"UPDATE bookings SET status='Confirmed' WHERE booking_id=? AND status NOT IN ('Completed','Cancelled')",booking);
    }
}
