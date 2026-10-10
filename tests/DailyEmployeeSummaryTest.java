import java.sql.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import qpal.dao.*;
import qpal.model.Account;
import qpal.util.DbConnection;

public class DailyEmployeeSummaryTest {
    public static void main(String[] args) throws Exception {
        String schema =
                "qpal_integration_test_" + java.util.UUID.randomUUID().toString().replace("-", "");
        try (Connection c = DbConnection.getConnection();
                Statement s = c.createStatement()) {
            s.executeUpdate("CREATE DATABASE " + schema);
            try {
                System.setProperty("qpal.db.url", "jdbc:mysql://localhost:3306/" + schema);
                try (Connection test = DbConnection.getConnection();
                        Statement seed = test.createStatement()) {
                    EmployeeStationDao.ensure(test);
                    seed.executeUpdate(
                            "CREATE TABLE accounts(id INT PRIMARY KEY,name VARCHAR(50),email"
                                + " VARCHAR(100),role VARCHAR(20))");
                    seed.executeUpdate(
                            "INSERT INTO accounts"
                                + " VALUES(1,'One','one@test','Employee'),(2,'Two','two@test','Employee'),(3,'Admin','admin@test','Admin')");
                    seed.executeUpdate(
                            "INSERT INTO"
                                + " employee_queue_work(event,queue_entry_id,account_id,kind,station,passengers,amount,created_at)"
                                + " VALUES ('Payment',1,1,'Payment',1,2,100,'2026-10-09"
                                + " 23:59:59'),('Served',1,1,'Payment',1,2,0,'2026-10-09"
                                + " 23:59:59'),('Boarded',1,1,'Boarding',1,2,0,'2026-10-09"
                                + " 23:59:59'),('Payment',2,1,'Payment',1,1,50,'2026-10-10"
                                + " 00:00:00'),('Boarded',3,1,'Payment',1,9,0,'2026-10-09"
                                + " 12:00:00'),('Served',4,1,'Boarding',1,9,0,'2026-10-09"
                                + " 12:00:00')");
                }
                var dao = new EmployeeSummaryDao();
                var admin = new Account(3, "Admin", "admin@test", "", "Admin", "Active");
                ActivityLogDao.setCurrentAccount(admin);
                try (Connection test = DbConnection.getConnection();
                        Statement seed = test.createStatement()) {
                    seed.executeUpdate(
                            "CREATE TABLE queue_entries(queue_entry_id INT PRIMARY KEY,booking_id"
                                + " INT)");
                    seed.executeUpdate("CREATE TABLE booking_passengers(booking_id INT)");
                    seed.executeUpdate(
                            "CREATE TABLE payments(payment_id INT,booking_id INT,amount"
                                + " DECIMAL(12,2))");
                    seed.executeUpdate("INSERT INTO queue_entries VALUES(10,10)");
                    seed.executeUpdate("INSERT INTO booking_passengers VALUES(10),(10)");
                    seed.executeUpdate("INSERT INTO payments VALUES(10,10,125)");
                    test.setAutoCommit(false);
                    EmployeeStationDao.recordWork(test, "Payment", "Payment", 2, 10);
                    EmployeeStationDao.recordWork(test, "Served", "Payment", 2, 10);
                    EmployeeStationDao.recordWork(test, "Boarded", "Boarding", 1, 10);
                    test.rollback();
                    try (ResultSet r =
                            seed.executeQuery(
                                    "SELECT COUNT(*) FROM employee_queue_work WHERE"
                                        + " account_id=3")) {
                        r.next();
                        if (r.getInt(1) != 0)
                            throw new AssertionError("Failed action attribution rolls back");
                    }
                    EmployeeStationDao.recordWork(test, "Payment", "Payment", 2, 10);
                    EmployeeStationDao.recordWork(test, "Served", "Payment", 2, 10);
                    EmployeeStationDao.recordWork(test, "Boarded", "Boarding", 1, 10);
                    try {
                        EmployeeStationDao.recordWork(test, "Boarded", "Boarding", 1, 10);
                        throw new AssertionError("Duplicate boarding accepted");
                    } catch (SQLException expected) {
                    }
                    seed.executeUpdate(
                            "UPDATE employee_queue_work SET created_at='2026-10-09 12:00:00' WHERE"
                                + " account_id=3");
                    test.commit();
                }
                ActivityLogDao.setCurrentAccount(null);
                var rows = dao.dailyEmployees(admin, LocalDate.of(2026, 10, 9));
                if (!rows.get(0).role().equals("Admin")
                        || rows.get(0).payments() != 1
                        || rows.get(0).transactionsMade() != 1
                        || rows.get(0).boarded() != 2
                        || rows.get(0).collected().compareTo(new BigDecimal("125")) != 0)
                    throw new AssertionError("Admin attribution and totals");
                if (rows.size() != 3
                        || rows.get(1).payments() != 1
                        || rows.get(1).collected().compareTo(new BigDecimal("100")) != 0
                        || rows.get(1).transactionsMade() != 1
                        || rows.get(1).boarded() != 2
                        || rows.get(2).payments() != 0)
                    throw new AssertionError("Daily totals or zero-work employee");
                if (dao.dailyEmployees(admin, LocalDate.of(2026, 10, 10))
                                .get(1)
                                .collected()
                                .compareTo(new BigDecimal("50"))
                        != 0) throw new AssertionError("Date boundary");
                try {
                    dao.dailyEmployees(
                            new Account(1, "One", "one@test", "", "Employee", "Active"),
                            LocalDate.now());
                    throw new AssertionError("Employee access");
                } catch (SQLException expected) {
                }
                System.out.println(
                        "PASS: all employees, zero activity, transaction counts and gate boarding"
                            + " totals, Manila date boundaries and admin-only access");
            } finally {
                System.clearProperty("qpal.db.url");
                s.executeUpdate("DROP DATABASE " + schema);
            }
        }
    }
}
