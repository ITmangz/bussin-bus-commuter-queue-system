package qpal.dao;

import java.sql.*;
import java.util.*;
import qpal.model.Account;
import qpal.model.EmployeeStation;
import qpal.util.DbConnection;
import static qpal.dao.BookingDao.*;

/** Exclusive, renewable station leases. A crashed client releases its station after 90 seconds. */
public final class EmployeeStationDao {
    public record Session(int accountId, EmployeeStation station, String token) {}
    private static volatile Session current;

    public static void activate(Session session) { current = session; }
    public static Session current() { return current; }
    public static boolean isEmployee() {
        Account account = ActivityLogDao.getCurrentAccount();
        return account != null && "Employee".equalsIgnoreCase(account.getRole());
    }

    public static void ensure(Connection c) throws SQLException {
        EmployeeSummaryDao.ensure(c);
        try (Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS employee_stations (kind VARCHAR(16) NOT NULL, station INT NOT NULL, "
                    + "account_id INT NULL, session_token VARCHAR(36) NULL, expires_at DATETIME NULL, PRIMARY KEY(kind,station)) ENGINE=InnoDB");
            s.executeUpdate("INSERT IGNORE INTO employee_stations(kind,station) VALUES('Payment',1),('Payment',2),('Boarding',1),('Boarding',2)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS employee_queue_work (event VARCHAR(16) NOT NULL, queue_entry_id INT NOT NULL, "
                    + "account_id INT NOT NULL, kind VARCHAR(16) NOT NULL, station INT NOT NULL, passengers INT NOT NULL DEFAULT 0, "
                    + "amount DECIMAL(12,2) NOT NULL DEFAULT 0, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "PRIMARY KEY(event,queue_entry_id), INDEX employee_work_date(account_id,created_at)) ENGINE=InnoDB");
        }
    }

    public Set<EmployeeStation> occupied() throws SQLException {
        try (Connection c = DbConnection.getConnection()) {
            ensure(c);
            Set<EmployeeStation> result = new HashSet<>();
            try (PreparedStatement p = c.prepareStatement("SELECT kind,station FROM employee_stations WHERE expires_at>NOW()"); ResultSet r = p.executeQuery()) {
                while (r.next()) result.add(new EmployeeStation(r.getString(1), r.getInt(2)));
            }
            return result;
        }
    }

    public Session claim(Account account, EmployeeStation station) throws SQLException {
        if (account == null || !"Employee".equalsIgnoreCase(account.getRole())) throw new SQLException("An employee account is required.");
        try (Connection c = DbConnection.getConnection()) {
            ensure(c);
            c.setAutoCommit(false);
            try {
                try (PreparedStatement p = statement(c,"SELECT id FROM accounts WHERE id=? AND role='Employee' AND status='Active' FOR UPDATE",account.getID()); ResultSet r=p.executeQuery()) {
                    if (!r.next()) throw new SQLException("This employee account is no longer active.");
                }
                // Lock all four rows in a fixed order, including concurrent claims by the same account.
                try (PreparedStatement p = c.prepareStatement("SELECT kind,station,account_id,expires_at>NOW() AS active FROM employee_stations ORDER BY kind,station FOR UPDATE"); ResultSet r=p.executeQuery()) {
                    while (r.next()) if (r.getBoolean("active")) {
                        if (r.getInt("account_id")==account.getID()) throw new SQLException("You already have an active station session. Log out there first.");
                        if (r.getString("kind").equals(station.kind()) && r.getInt("station")==station.number()) throw new SQLException("This station is in use. Choose another station.");
                    }
                }
                Session session = new Session(account.getID(),station,UUID.randomUUID().toString());
                EmployeeSummaryDao.start(c,session);
                update(c,"UPDATE employee_stations SET account_id=?,session_token=?,expires_at=DATE_ADD(NOW(),INTERVAL 90 SECOND) WHERE kind=? AND station=?",
                        session.accountId(),session.token(),station.kind(),station.number());
                c.commit();
                new ActivityLogDao().addActivity(account,"Queue Management","Update","Started session at " + station.title() + ".");
                return session;
            } catch (SQLException | RuntimeException ex) { c.rollback(); throw ex; }
        }
    }

    public void heartbeat(Session session) throws SQLException {
        try (Connection c=DbConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
            int changed=update(c,"UPDATE employee_stations es JOIN accounts a ON a.id=es.account_id SET es.expires_at=DATE_ADD(NOW(),INTERVAL 90 SECOND) "
                    + "WHERE es.session_token=? AND es.account_id=? AND es.expires_at>NOW() AND a.role='Employee' AND a.status='Active'",session.token(),session.accountId());
            if (changed!=1) throw new SQLException("Your station session has ended. Select an available station again.");
            if(update(c,"UPDATE employee_work_sessions SET last_seen=NOW() WHERE token=? AND ended_at IS NULL",session.token())!=1)
                throw new SQLException("Your work session has ended. Select your station again.");
            c.commit();
            } catch(SQLException | RuntimeException ex) { c.rollback(); throw ex; }
        }
    }

    public void release(Session session) throws SQLException {
        if (session==null) return;
        try (Connection c=DbConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
            update(c,"UPDATE employee_stations SET account_id=NULL,session_token=NULL,expires_at=NULL WHERE session_token=? AND account_id=?",session.token(),session.accountId());
            update(c,"UPDATE employee_work_sessions SET ended_at=COALESCE(ended_at,NOW()) WHERE token=?",session.token());
            c.commit();
            } catch(SQLException | RuntimeException ex) { c.rollback(); throw ex; }
        }
    }

    /** Called inside the queue transaction, so a station cannot be reassigned during an action. */
    public static void requireStation(Connection c,String kind,int station) throws SQLException {
        if (!isEmployee()) return;
        Session session=current;
        Account account=ActivityLogDao.getCurrentAccount();
        if (session==null || session.accountId()!=account.getID() || !session.station().kind().equals(kind) || session.station().number()!=station)
            throw new SQLException("Use your assigned station for this action.");
        try (PreparedStatement p=statement(c,"SELECT es.station FROM employee_stations es JOIN accounts a ON a.id=es.account_id "
                + "WHERE es.kind=? AND es.station=? AND es.account_id=? AND es.session_token=? AND es.expires_at>NOW() "
                + "AND a.status='Active' AND a.role='Employee' FOR UPDATE",kind,station,session.accountId(),session.token()); ResultSet r=p.executeQuery()) {
            if (!r.next()) throw new SQLException("Your station session expired. Return to station selection.");
        }
    }

    public static void requireQueue(Connection c,String kind,int queue) throws SQLException {
        if (!isEmployee()) return;
        Session session=current;
        if (session==null) throw new SQLException("Select your station first.");
        requireStation(c,kind,session.station().number());
        try (PreparedStatement p=statement(c,"SELECT queue_entry_id FROM queue_stations WHERE kind=? AND station=? AND queue_entry_id=? FOR UPDATE",kind,session.station().number(),queue); ResultSet r=p.executeQuery()) {
            if (!r.next()) throw new SQLException("This queue is not assigned to your station.");
        }
    }

    /** Writes attribution in the same transaction as the successful business action. */
    public static void recordWork(Connection c,String event,String kind,int station,int queue) throws SQLException {
        Account actor = ActivityLogDao.getCurrentAccount();
        if (actor == null || (!"Employee".equalsIgnoreCase(actor.getRole()) && !"Admin".equalsIgnoreCase(actor.getRole()))) return;
        Session session = "Employee".equalsIgnoreCase(actor.getRole()) ? current : null;
        if ("Employee".equalsIgnoreCase(actor.getRole()) && (session == null || session.accountId() != actor.getID()))
            throw new SQLException("Select your station first.");
        update(c,"INSERT INTO employee_queue_work(event,queue_entry_id,account_id,kind,station,passengers,amount) "
                + "SELECT ?,q.queue_entry_id,?,?,?,(SELECT COUNT(*) FROM booking_passengers bp WHERE bp.booking_id=q.booking_id),"
                + "CASE WHEN ?='Payment' THEN COALESCE((SELECT amount FROM payments WHERE booking_id=q.booking_id ORDER BY payment_id DESC LIMIT 1),0) ELSE 0 END "
                + "FROM queue_entries q WHERE q.queue_entry_id=?",event,actor.getID(),kind,station,event,queue);
        if (session != null) EmployeeSummaryDao.credit(c,session,event,queue);
    }
}
