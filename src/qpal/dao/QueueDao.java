package qpal.dao;

import java.sql.*;
import java.math.BigDecimal;
import java.util.*;
import qpal.model.BookingData.*;
import qpal.util.DbConnection;
import static qpal.dao.BookingDao.*;

public class QueueDao {
    static void ensureStations(Connection c) throws SQLException {
        try (Statement s=c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS queue_stations (kind VARCHAR(16) NOT NULL, station INT NOT NULL, queue_entry_id INT NOT NULL, PRIMARY KEY(kind,station), UNIQUE KEY assigned_queue(kind,queue_entry_id)) ENGINE=InnoDB");
        }
    }

    public Map<Integer,Integer> stations(String kind) throws SQLException {
        try (Connection c=DbConnection.getConnection()) {
            ensureStations(c);
            Map<Integer,Integer> result=new HashMap<>();
            try (PreparedStatement p=statement(c,"SELECT station,queue_entry_id FROM queue_stations WHERE kind=?",kind); ResultSet r=p.executeQuery()) {
                while(r.next()) result.put(r.getInt(1),r.getInt(2));
            }
            return result;
        }
    }

    public void callBoarding(int queueId, int station, boolean skip) throws SQLException {
        if (station<1 || station>2) throw new SQLException("Select a gate first.");
        // Fetch eligibility before taking the shared transition mutex.
        var eligible=boarding();
        try(Connection c=DbConnection.getConnection()) {
            ensureStations(c);
            c.setAutoCommit(false);
            try {
                EmployeeStationDao.requireStation(c,"Boarding",station);
                int trip=BoardingGateDao.assignedTrip(c,station);
                update(c,"INSERT INTO queue_daily_counters(queue_date,last_queue_number) VALUES(CURRENT_DATE,0) ON DUPLICATE KEY UPDATE last_queue_number=last_queue_number");
                Map<Integer,Integer> assigned=new HashMap<>();
                try(PreparedStatement p=c.prepareStatement("SELECT station,queue_entry_id FROM queue_stations WHERE kind='Boarding' FOR UPDATE"); ResultSet r=p.executeQuery()) {
                    while(r.next()) assigned.put(r.getInt(1),r.getInt(2));
                }

                if(skip) {
                    Integer current=assigned.get(station);
                    if(current!=null) update(c,"REPLACE INTO boarding_skips(queue_entry_id,skipped_at) VALUES(?,NOW())",current);
                    update(c,"DELETE FROM queue_stations WHERE kind='Boarding' AND station=?",station);
                }
                else {
                    Integer current=assigned.get(station);
                    if(eligible.stream().anyMatch(r -> Objects.equals(current,r.id()))) throw new SQLException("Complete or skip the current gate queue first.");
                    java.util.Set<Integer> tripQueues=new java.util.HashSet<>();
                    try(PreparedStatement p=statement(c,"SELECT q.queue_entry_id FROM queue_entries q JOIN bookings b ON b.booking_id=q.booking_id WHERE b.trip_id=? AND q.status='Completed' AND b.status NOT IN ('Cancelled','Expired','No-show') AND NOT EXISTS(SELECT 1 FROM queue_boarding qb WHERE qb.queue_entry_id=q.queue_entry_id) FOR UPDATE",trip);ResultSet r=p.executeQuery()) {while(r.next()) tripQueues.add(r.getInt(1));}
                    var next=eligible.stream().filter(r -> tripQueues.contains(r.id())).filter(r -> !assigned.containsValue(r.id()) && (queueId==0 || queueId==r.id())).findFirst()
                            .orElseThrow(() -> new SQLException("No unassigned boarding queues are available."));
                    update(c,"REPLACE INTO queue_stations(kind,station,queue_entry_id) VALUES('Boarding',?,?)",station,next.id());
                }
                c.commit();
                ActivityLogDao.recordActivity("Queue Management", "Update", (skip ? "Skipped" : "Called") + " boarding queue #" + queueId + " at gate " + station + ".");
            } catch(SQLException | RuntimeException ex) { c.rollback(); throw ex; }
        }
    }
    static void ensureBoardingTable(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS queue_boarding ("
                    + "queue_entry_id INT PRIMARY KEY, boarded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (queue_entry_id) REFERENCES queue_entries(queue_entry_id) ON DELETE CASCADE) ENGINE=InnoDB");
        }
    }

    public List<QueueRow> boarding() throws SQLException {
        try (Connection c = DbConnection.getConnection()) {
            ensureBoardingTable(c);
            BoardingGateDao.ensure(c);
            qpal.util.DepartureService.reconcile(c);
            String sql = "SELECT q.queue_entry_id,q.booking_id,q.queue_number,MIN(bp.passenger_name),r.origin,r.destination,b.bus_number,"
                    + "t.departure_date,t.departure_time,COUNT(*),CASE WHEN EXISTS(SELECT 1 FROM boarding_gates g WHERE g.trip_id=t.trip_id) THEN 'Boarding' ELSE 'Awaiting Gate' END AS boarding_status "
                    + "FROM queue_entries q JOIN bookings bk ON bk.booking_id=q.booking_id "
                    + "JOIN trips t ON t.trip_id=bk.trip_id JOIN routes r ON r.route_id=t.route_id "
                    + "JOIN buses b ON b.bus_id=t.bus_id JOIN booking_passengers bp ON bp.booking_id=bk.booking_id "
                    + "WHERE t.status IN ('Scheduled','Boarding') AND q.status='Completed' AND bk.status NOT IN ('Cancelled','Expired','No-show') "
                    + "AND NOT EXISTS (SELECT 1 FROM queue_boarding qb WHERE qb.queue_entry_id=q.queue_entry_id) "
                    + "AND (SELECT p.status FROM payments p WHERE p.booking_id=bk.booking_id "
                    + "ORDER BY p.payment_id DESC LIMIT 1)='Paid' "
                    + "GROUP BY q.queue_entry_id,q.booking_id,q.queue_number,r.origin,r.destination,b.bus_number,t.departure_date,t.departure_time "
                    + "ORDER BY t.departure_date,t.departure_time,t.trip_id,COALESCE((SELECT skipped_at FROM boarding_skips bs WHERE bs.queue_entry_id=q.queue_entry_id),(SELECT MAX(paid_at) FROM payments p WHERE p.booking_id=bk.booking_id)),q.queue_entry_id";
            List<QueueRow> rows = new ArrayList<>();
            try (PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
                while (r.next()) rows.add(new QueueRow(r.getInt(1),r.getInt(2),r.getInt(3),
                        r.getString(5) + " - " + r.getString(6),r.getString(7),
                        r.getString(8) + " " + r.getString(9),r.getString(4),"Paid",r.getString("boarding_status"),r.getInt(10)));
            }
            return rows;
        }
    }

    public void recallBoarding(int queueId,int station) throws SQLException {
        try (Connection c=DbConnection.getConnection()) {
            ensureStations(c); ensureBoardingTable(c); c.setAutoCommit(false);
            try {
                EmployeeStationDao.requireStation(c,"Boarding",station);
                try (PreparedStatement p=statement(c,"SELECT s.queue_entry_id FROM queue_stations s WHERE s.kind='Boarding' AND s.station=? AND s.queue_entry_id=? "
                        + "AND NOT EXISTS(SELECT 1 FROM queue_boarding b WHERE b.queue_entry_id=s.queue_entry_id) FOR UPDATE",station,queueId); ResultSet r=p.executeQuery()) {
                    if (!r.next()) throw new SQLException("This queue is no longer being called at your gate.");
                }
                c.commit();
                ActivityLogDao.recordActivity("Queue Management","Update","Recalled boarding queue #"+queueId+" at gate "+station+".");
            } catch (SQLException | RuntimeException ex) { c.rollback(); throw ex; }
        }
    }

    public void completeBoarding(int queueId) throws SQLException {
        try (Connection c = DbConnection.getConnection()) {
            ensureBoardingTable(c);
            BoardingGateDao.ensure(c);
            ensureStations(c);
            QueuePaymentDao.ensureTable(c);
            c.setAutoCommit(false);
            try {
                EmployeeStationDao.requireQueue(c,"Boarding",queueId);
                try (PreparedStatement p = statement(c,
                        "SELECT q.queue_entry_id FROM queue_entries q JOIN bookings bk ON bk.booking_id=q.booking_id "
                        + "JOIN trips t ON t.trip_id=bk.trip_id WHERE q.queue_entry_id=? "
                        + "AND t.status='Boarding' AND EXISTS(SELECT 1 FROM boarding_gates g JOIN queue_stations s ON s.station=g.gate AND s.kind='Boarding' WHERE g.trip_id=t.trip_id AND s.queue_entry_id=q.queue_entry_id) "
                        + "AND TIMESTAMP(t.departure_date,t.departure_time)>NOW() "
                        + "AND q.status='Completed' AND bk.status NOT IN ('Cancelled','Expired','No-show') "
                        + "AND (SELECT status FROM payments WHERE booking_id=bk.booking_id ORDER BY payment_id DESC LIMIT 1)='Paid' "
                        + "FOR UPDATE", queueId); ResultSet r = p.executeQuery()) {
                    if (!r.next()) throw new SQLException("This queue is no longer eligible for boarding. Refresh and try again.");
                }
                QueuePaymentDao.requirePrinted(c,queueId);
                if (update(c,"INSERT IGNORE INTO queue_boarding(queue_entry_id) VALUES (?)",queueId)==0)
                    throw new SQLException("Boarding has already been completed for this queue.");
                try (PreparedStatement p = statement(c,"SELECT station FROM queue_stations WHERE kind='Boarding' AND queue_entry_id=?",queueId);
                        ResultSet r = p.executeQuery()) {
                    if (!r.next()) throw new SQLException("This queue is not assigned to a boarding gate.");
                    EmployeeStationDao.recordWork(c,"Boarded","Boarding",r.getInt(1),queueId);
                }
                c.commit();
                ActivityLogDao.recordActivity("Queue Management", "Update", "Marked queue #" + queueId + " as boarded.");
            } catch (SQLException | RuntimeException ex) { c.rollback(); throw ex; }
        }
    }

    public List<QueueRow> today() throws SQLException {
        String sql = "SELECT q.*,CASE WHEN bk.status IN ('No-show','Expired') OR q.status='Expired' THEN 'No-show' ELSE q.status END AS display_status,r.origin,r.destination,b.bus_number,t.departure_date,t.departure_time,"
                + "(SELECT MIN(passenger_name) FROM booking_passengers WHERE booking_id=q.booking_id) AS passenger,"
                + "(SELECT COUNT(*) FROM booking_passengers WHERE booking_id=q.booking_id) AS passengers,"
                + "(SELECT status FROM payments WHERE booking_id=q.booking_id ORDER BY payment_id DESC LIMIT 1) AS payment "
                + "FROM queue_entries q JOIN bookings bk ON bk.booking_id=q.booking_id JOIN trips t ON t.trip_id=bk.trip_id "
                + "JOIN routes r ON r.route_id=t.route_id JOIN buses b ON b.bus_id=t.bus_id "
                + "WHERE q.queue_date=CURRENT_DATE ORDER BY q.queue_number";
        try (Connection c = DbConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            List<QueueRow> rows = new ArrayList<>();
            while (r.next()) rows.add(new QueueRow(r.getInt("queue_entry_id"), r.getInt("booking_id"),
                    r.getInt("queue_number"), r.getString("origin") + " - " + r.getString("destination"),
                    r.getString("bus_number"), r.getString("departure_date") + " " + r.getString("departure_time"),
                    r.getString("passenger"), "No-show".equals(r.getString("display_status"))
                            && "Cancelled".equals(r.getString("payment")) ? "Unpaid" : r.getString("payment"),
                    r.getString("display_status"), r.getInt("passengers")));
            return rows;
        }
    }

    public void act(int queueId, String action) throws SQLException { act(queueId, action, 1); }
    public void act(int queueId, String action, int station) throws SQLException {
        if (station < 1 || station > 2) throw new SQLException("Select a counter first.");
        try (Connection c = DbConnection.getConnection()) {
            ensureStations(c);
            QueuePaymentDao.ensureTable(c);
            c.setAutoCommit(false);
            try {
                EmployeeStationDao.requireStation(c,"Payment",station);
                if (EmployeeStationDao.isEmployee() && !action.equals("Call Next Queue") && !action.equals("Recall"))
                    EmployeeStationDao.requireQueue(c,"Payment",queueId);
                if (action.equals("Mark as Paid")) PaymentDeadlineDao.requireOpen(c, queueId);
                // The day's counter is also a mutex for admin queue transitions.
                update(c, "INSERT INTO queue_daily_counters (queue_date,last_queue_number) VALUES (CURRENT_DATE,0) "
                        + "ON DUPLICATE KEY UPDATE last_queue_number=last_queue_number");
                if (action.equals("Call Next Queue") || action.equals("Recall")) {
                    try (PreparedStatement p = c.prepareStatement("SELECT queue_entry_id FROM queue_entries "
                            + "WHERE queue_date=CURRENT_DATE AND status='Serving' AND queue_entry_id IN (SELECT queue_entry_id FROM queue_stations WHERE kind='Payment' AND station=" + station + ") FOR UPDATE"); ResultSet r = p.executeQuery()) {
                        if (r.next() && !(action.equals("Recall") && r.getInt(1)==queueId))
                            throw new SQLException("Complete or skip the currently serving queue first.");
                    }
                }
                if (action.equals("Call Next Queue")) {
                    try (PreparedStatement p = c.prepareStatement("SELECT queue_entry_id FROM queue_entries "
                            + "WHERE queue_date=CURRENT_DATE AND status IN ('Waiting','Skipped') "
                            + "ORDER BY CASE WHEN status='Waiting' THEN 0 ELSE 1 END,queue_number LIMIT 1 FOR UPDATE");
                            ResultSet r = p.executeQuery()) {
                        if (!r.next()) throw new SQLException("There are no waiting or skipped queues.");
                        queueId = r.getInt(1);
                    }
                }
                int booking;
                String status;
                try (PreparedStatement p = statement(c, "SELECT booking_id,status FROM queue_entries "
                        + "WHERE queue_entry_id=? AND queue_date=CURRENT_DATE FOR UPDATE", queueId); ResultSet r = p.executeQuery()) {
                    if (!r.next()) throw new SQLException("Select a queue for today first.");
                    booking = r.getInt(1); status = r.getString(2);
                    if (status.equals("Serving")) {
                        try (PreparedStatement owner = statement(c,"SELECT station FROM queue_stations WHERE kind='Payment' AND queue_entry_id=?",queueId); ResultSet o=owner.executeQuery()) {
                            if (o.next() && o.getInt(1)!=station) throw new SQLException("This queue belongs to another counter.");
                        }
                    }
                }
                switch (action) {
                    case "Call Next Queue": case "Recall":
                        if (action.equals("Recall") && status.equals("Serving")) {
                            update(c,"UPDATE queue_entries SET called_at=NOW() WHERE queue_entry_id=?",queueId);
                            break;
                        }
                        if (!(status.equals("Waiting") || status.equals("Skipped"))) throw new SQLException("This queue cannot be called.");
                        if (action.equals("Recall") && !status.equals("Skipped")) {
                            try (PreparedStatement p = statement(c, "SELECT queue_entry_id FROM queue_entries "
                                    + "WHERE queue_date=CURRENT_DATE AND status IN ('Waiting','Skipped') "
                                    + "ORDER BY queue_number LIMIT 1 FOR UPDATE"); ResultSet r = p.executeQuery()) {
                                if (!r.next() || r.getInt(1) != queueId)
                                    throw new SQLException("Recall the earliest waiting or skipped queue first to preserve FIFO.");
                            }
                        }
                        update(c, "UPDATE queue_entries SET status='Serving',called_at=NOW() WHERE queue_entry_id=?", queueId);
                        update(c, "REPLACE INTO queue_stations(kind,station,queue_entry_id) VALUES ('Payment',?,?)",station,queueId);
                        break;
                    case "Undo Call":
                        if (!status.equals("Serving")) throw new SQLException("Only the current call can be undone.");
                        update(c, "UPDATE queue_entries SET status='Waiting',called_at=NULL WHERE queue_entry_id=?", queueId);
                        break;
                    case "Skip Queue":
                        if (!status.equals("Serving")) throw new SQLException("Only the serving queue can be skipped.");
                        update(c, "UPDATE queue_entries SET status='Skipped' WHERE queue_entry_id=?", queueId);
                        break;
                    case "Mark as Paid":
                        PaymentDeadlineDao.requireOpen(c, queueId);
                        try (PreparedStatement p = statement(c,"SELECT payment_method FROM payments WHERE booking_id=? ORDER BY payment_id DESC LIMIT 1",booking);
                                ResultSet r = p.executeQuery()) {
                            if (r.next() && !"Cash".equals(r.getString(1)))
                                throw new SQLException("Open Payment to verify the cashless transaction and record its reference.");
                        }
                        if (Set.of("Cancelled", "Completed", "Expired", "No-show").contains(status)) throw new SQLException("This queue is closed.");
                        if (update(c, "UPDATE payments SET status='Paid',paid_at=NOW() WHERE booking_id=? AND status='Pending'", booking) == 0)
                            throw new SQLException("There is no pending payment for this booking.");
                        update(c, "UPDATE bookings SET status='Confirmed' WHERE booking_id=?", booking);
                        EmployeeStationDao.recordWork(c,"Payment","Payment",station,queueId);
                        break;
                    case "Complete":
                        if (!status.equals("Serving")) throw new SQLException("Only the serving queue can be completed.");
                        QueuePaymentDao.requirePrinted(c,queueId);
                        try (PreparedStatement p = statement(c, "SELECT status FROM payments WHERE booking_id=? ORDER BY payment_id DESC LIMIT 1 FOR UPDATE", booking);
                                ResultSet r = p.executeQuery()) {
                            if (!r.next() || !r.getString(1).equals("Paid")) throw new SQLException("Collect and mark the payment as Paid first.");
                        }
                        update(c, "UPDATE queue_entries SET status='Completed',completed_at=NOW() WHERE queue_entry_id=?", queueId);
                        update(c, "UPDATE bookings SET status='Completed' WHERE booking_id=?", booking);
                        EmployeeStationDao.recordWork(c,"Served","Payment",station,queueId);
                        break;
                    default: throw new SQLException("Unknown queue action.");
                }
                c.commit();
                ActivityLogDao.recordActivity("Queue Management", "Update", action + " for queue #" + queueId + " at counter " + station + ".");
            } catch (SQLException | RuntimeException ex) { c.rollback(); throw ex; }
        }
    }

    public RevenueData revenue() throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        int paid = 0, pending = 0;
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal allTimeTotal = BigDecimal.ZERO;
        java.util.Set<Integer> countedPayments = new java.util.HashSet<>();
        java.util.Set<Integer> pendingPayments = new java.util.HashSet<>();
        String sql = "SELECT p.*,b.bus_number,r.origin,r.destination,bp.passenger_name,bp.fare,q.queue_number,"
                + "(SELECT sr.seat_number FROM seat_reservations sr WHERE sr.booking_passenger_id=bp.booking_passenger_id "
                + "ORDER BY sr.seat_reservation_id DESC LIMIT 1) AS seat_number,"
                + "DATE(p.paid_at)=CURRENT_DATE AS paid_today FROM payments p JOIN trips t ON t.trip_id=p.trip_id "
                + "JOIN buses b ON b.bus_id=t.bus_id JOIN routes r ON r.route_id=t.route_id "
                + "LEFT JOIN booking_passengers bp ON bp.booking_id=p.booking_id "
                + "LEFT JOIN queue_entries q ON q.booking_id=p.booking_id "
                + "ORDER BY CASE p.status WHEN 'Pending' THEN 0 WHEN 'Paid' THEN 2 ELSE 1 END,"
                + "p.created_at DESC,p.payment_id DESC,bp.booking_passenger_id";
        try (Connection c = DbConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            while (r.next()) {
                String status = r.getString("status");
                BigDecimal amount = r.getBigDecimal("amount");
                String passenger = r.getString("passenger_name");
                String queue = r.getObject("queue_number") == null ? "—"
                        : String.format(java.util.Locale.ROOT, "P%03d", r.getInt("queue_number"));
                String seat = r.getObject("seat_number") == null ? "—" : BookingDao.seatLabel(r.getInt("seat_number"));
                rows.add(new Object[]{r.getInt("payment_id"), queue, r.getString("bus_number"),
                        r.getString("origin") + " - " + r.getString("destination"), r.getString("created_at"),
                        passenger == null ? amount : r.getBigDecimal("fare"), status, seat});
                if (status.equals("Paid")) {
                    paid++;
                    if (countedPayments.add(r.getInt("payment_id"))) {
                        allTimeTotal = allTimeTotal.add(amount);
                        if (r.getBoolean("paid_today")) total = total.add(amount);
                    }
                }
                if (status.equals("Pending") && pendingPayments.add(r.getInt("payment_id"))) pending++;
            }
        }
        return new RevenueData(rows, paid, pending, total, allTimeTotal);
    }
}
