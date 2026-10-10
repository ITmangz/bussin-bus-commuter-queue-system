package qpal.dao;

import java.sql.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.*;
import qpal.util.DbConnection;
import static qpal.dao.BookingDao.*;

/** Durable session totals and once-only daily activity summaries. */
public final class EmployeeSummaryDao {
    public record DailyEmployee(
            int id,
            String name,
            String email,
            String role,
            int payments,
            BigDecimal collected,
            int transactionsMade,
            int boarded) {}

    public List<DailyEmployee> dailyEmployees(qpal.model.Account viewer, LocalDate day)
            throws SQLException {
        if (viewer == null || !"Admin".equalsIgnoreCase(viewer.getRole()))
            throw new SQLException("Only administrators can view all staff summaries.");
        List<DailyEmployee> rows = new ArrayList<>();
        try (Connection c = DbConnection.getConnection()) {
            EmployeeStationDao.ensure(c);
            try (PreparedStatement p =
                            statement(
                                    c,
                                    "SELECT a.id,a.name,a.email,a.role,"
                                        + "COALESCE(SUM(w.event='Payment'),0),COALESCE(SUM(CASE"
                                        + " WHEN w.event='Payment' THEN w.amount ELSE 0"
                                        + " END),0),COALESCE(SUM(CASE WHEN w.event='Served' AND"
                                        + " w.kind='Payment' THEN 1 ELSE 0"
                                        + " END),0),COALESCE(SUM(CASE WHEN w.event='Boarded' AND"
                                        + " w.kind='Boarding' THEN w.passengers ELSE 0 END),0) FROM"
                                        + " accounts a LEFT JOIN employee_queue_work w ON"
                                        + " w.account_id=a.id AND w.created_at>=? AND"
                                        + " w.created_at<? WHERE a.role IN ('Employee','Admin')"
                                        + " GROUP BY a.id,a.name,a.email,a.role ORDER BY"
                                        + " a.name,a.id",
                                    java.sql.Date.valueOf(day),
                                    java.sql.Date.valueOf(day.plusDays(1)));
                    ResultSet r = p.executeQuery()) {
                while (r.next())
                    rows.add(
                            new DailyEmployee(
                                    r.getInt(1),
                                    r.getString(2),
                                    r.getString(3),
                                    r.getString(4),
                                    r.getInt(5),
                                    r.getBigDecimal(6),
                                    r.getInt(7),
                                    r.getInt(8)));
            }
        }
        return rows;
    }

    public java.time.LocalDateTime sessionStarted(EmployeeStationDao.Session session)
            throws SQLException {
        try (Connection c = DbConnection.getConnection();
                PreparedStatement p =
                        statement(
                                c,
                                "SELECT started_at FROM employee_work_sessions WHERE token=? AND"
                                    + " account_id=?",
                                session.token(),
                                session.accountId());
                ResultSet r = p.executeQuery()) {
            return r.next() ? r.getTimestamp(1).toLocalDateTime() : null;
        }
    }

    public static void ensure(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS employee_work_sessions (token VARCHAR(36) PRIMARY"
                        + " KEY, account_id INT NOT NULL, kind VARCHAR(16) NOT NULL, station INT"
                        + " NOT NULL, started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, last_seen"
                        + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, ended_at TIMESTAMP NULL, collected"
                        + " DECIMAL(12,2) NOT NULL DEFAULT 0, commuters INT NOT NULL DEFAULT 0,"
                        + " logout_logged BOOLEAN NOT NULL DEFAULT FALSE) ENGINE=InnoDB");
            s.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS employee_daily_summaries (account_id INT NOT NULL,"
                        + " work_date DATE NOT NULL, PRIMARY KEY(account_id,work_date))"
                        + " ENGINE=InnoDB");
        }
    }

    public static void start(Connection c, EmployeeStationDao.Session session) throws SQLException {
        update(
                c,
                "INSERT INTO employee_work_sessions(token,account_id,kind,station) VALUES(?,?,?,?)",
                session.token(),
                session.accountId(),
                session.station().kind(),
                session.station().number());
    }

    public static void credit(
            Connection c, EmployeeStationDao.Session session, String event, int queue)
            throws SQLException {
        int changed =
                update(
                        c,
                        "UPDATE employee_work_sessions s JOIN employee_queue_work w ON w.event=?"
                            + " AND w.queue_entry_id=? SET s.collected=s.collected+CASE WHEN"
                            + " w.event='Payment' THEN w.amount ELSE 0 END,"
                            + " s.commuters=s.commuters+CASE WHEN w.event IN ('Served','Boarded')"
                            + " THEN w.passengers ELSE 0 END WHERE s.token=? AND"
                            + " s.account_id=w.account_id AND s.ended_at IS NULL",
                        event,
                        queue,
                        session.token());
        if (changed != 1)
            throw new SQLException("Your work session has ended. Select your station again.");
    }

    public void logout(EmployeeStationDao.Session session) throws SQLException {
        if (session == null) return;
        try (Connection c = DbConnection.getConnection()) {
            ensure(c);
            c.setAutoCommit(false);
            try {
                // Same lock order as queue actions; finish attribution before logging/releasing.
                try (PreparedStatement p =
                                statement(
                                        c,
                                        "SELECT station FROM employee_stations WHERE kind=? AND"
                                            + " station=? FOR UPDATE",
                                        session.station().kind(),
                                        session.station().number());
                        ResultSet r = p.executeQuery()) {
                    while (r.next()) {}
                }
                BigDecimal collected;
                int commuters;
                boolean logged;
                try (PreparedStatement p =
                                statement(
                                        c,
                                        "SELECT collected,commuters,logout_logged FROM"
                                            + " employee_work_sessions WHERE token=? AND"
                                            + " account_id=? FOR UPDATE",
                                        session.token(),
                                        session.accountId());
                        ResultSet r = p.executeQuery()) {
                    if (!r.next())
                        throw new SQLException("Work session not found. Please retry logout.");
                    collected = r.getBigDecimal(1);
                    commuters = r.getInt(2);
                    logged = r.getBoolean(3);
                }
                if (!logged) {
                    BigDecimal today;
                    try (PreparedStatement p =
                                    statement(
                                            c,
                                            "SELECT COALESCE(SUM(amount),0) FROM"
                                                + " employee_queue_work WHERE account_id=? AND"
                                                + " event='Payment' AND created_at>=CURRENT_DATE"
                                                + " AND created_at<CURRENT_DATE+INTERVAL 1 DAY",
                                            session.accountId());
                            ResultSet r = p.executeQuery()) {
                        r.next();
                        today = r.getBigDecimal(1);
                    }
                    log(
                            c,
                            session.accountId(),
                            "Logout",
                            "Logout summary: "
                                    + session.station().title()
                                    + " • Session collected: PHP "
                                    + collected.toPlainString()
                                    + " • Today's total: PHP "
                                    + today.toPlainString()
                                    + " • Session commuters served: "
                                    + commuters
                                    + ".");
                    update(
                            c,
                            "UPDATE employee_work_sessions SET"
                                + " ended_at=COALESCE(ended_at,NOW()),logout_logged=TRUE WHERE"
                                + " token=?",
                            session.token());
                }
                update(
                        c,
                        "UPDATE employee_stations SET"
                            + " account_id=NULL,session_token=NULL,expires_at=NULL WHERE"
                            + " session_token=? AND account_id=?",
                        session.token(),
                        session.accountId());
                c.commit();
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    private static void log(Connection c, int account, String action, String description)
            throws SQLException {
        if (update(
                        c,
                        "INSERT INTO"
                            + " activity_logs(account_id,user_name,email,role,module,action,description)"
                            + " SELECT id,name,email,role,'Employee Collections',?,? FROM accounts"
                            + " WHERE id=?",
                        action,
                        description,
                        account)
                != 1) throw new SQLException("Unable to save employee summary.");
    }

    public void reconcile() throws SQLException {
        try (Connection c = DbConnection.getConnection()) {
            EmployeeStationDao.ensure(c);
            c.setAutoCommit(false);
            try {
                // Wait for any pre-midnight payment/completion to commit before finalizing its day.
                try (PreparedStatement p =
                                c.prepareStatement(
                                        "SELECT kind,station FROM employee_stations ORDER BY"
                                            + " kind,station FOR UPDATE");
                        ResultSet r = p.executeQuery()) {
                    while (r.next()) {}
                }
                update(
                        c,
                        "UPDATE employee_work_sessions SET ended_at=DATE_ADD(last_seen,INTERVAL 90"
                            + " SECOND) WHERE ended_at IS NULL AND"
                            + " last_seen<DATE_SUB(NOW(),INTERVAL 90 SECOND)");
                LocalDate today;
                try (Statement s = c.createStatement();
                        ResultSet r = s.executeQuery("SELECT CURRENT_DATE")) {
                    r.next();
                    today = r.getDate(1).toLocalDate();
                }
                Map<Integer, Set<LocalDate>> days = new HashMap<>();
                try (Statement s = c.createStatement();
                        ResultSet r =
                                s.executeQuery(
                                        "SELECT DISTINCT account_id,DATE(created_at) FROM"
                                            + " employee_queue_work WHERE"
                                            + " created_at<CURRENT_DATE")) {
                    while (r.next())
                        days.computeIfAbsent(r.getInt(1), k -> new TreeSet<>())
                                .add(r.getDate(2).toLocalDate());
                }
                // Include days with a station session but no collections, including overnight
                // sessions.
                try (Statement s = c.createStatement();
                        ResultSet r =
                                s.executeQuery(
                                        "SELECT"
                                            + " account_id,DATE(started_at),DATE(COALESCE(ended_at,NOW()))"
                                            + " FROM employee_work_sessions WHERE"
                                            + " started_at<CURRENT_DATE")) {
                    while (r.next()) {
                        LocalDate end = r.getDate(3).toLocalDate();
                        for (LocalDate d = r.getDate(2).toLocalDate();
                                !d.isAfter(end) && d.isBefore(today);
                                d = d.plusDays(1))
                            days.computeIfAbsent(r.getInt(1), k -> new TreeSet<>()).add(d);
                    }
                }
                for (var employee : days.entrySet())
                    for (LocalDate day : employee.getValue()) {
                        if (update(
                                        c,
                                        "INSERT IGNORE INTO"
                                            + " employee_daily_summaries(account_id,work_date)"
                                            + " VALUES(?,?)",
                                        employee.getKey(),
                                        java.sql.Date.valueOf(day))
                                == 0) continue;
                        try (PreparedStatement p =
                                        statement(
                                                c,
                                                "SELECT COALESCE(SUM(CASE WHEN event='Payment' THEN"
                                                    + " amount ELSE 0 END),0),COALESCE(SUM(CASE"
                                                    + " WHEN event IN ('Served','Boarded') THEN"
                                                    + " passengers ELSE 0 END),0) FROM"
                                                    + " employee_queue_work WHERE account_id=? AND"
                                                    + " created_at>=? AND created_at<?",
                                                employee.getKey(),
                                                java.sql.Date.valueOf(day),
                                                java.sql.Date.valueOf(day.plusDays(1)));
                                ResultSet r = p.executeQuery()) {
                            r.next();
                            log(
                                    c,
                                    employee.getKey(),
                                    "Daily Summary",
                                    "Daily summary — "
                                            + day
                                            + ": Total collected: PHP "
                                            + r.getBigDecimal(1).toPlainString()
                                            + " • Commuters served: "
                                            + r.getInt(2)
                                            + ".");
                        }
                    }
                c.commit();
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        }
    }
}
