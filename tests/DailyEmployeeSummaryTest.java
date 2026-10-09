import java.sql.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import qpal.dao.*;
import qpal.model.Account;
import qpal.util.DbConnection;
public class DailyEmployeeSummaryTest {
 public static void main(String[] args)throws Exception {
 String schema="qpal_integration_test_"+java.util.UUID.randomUUID().toString().replace("-","");
 try(Connection c=DbConnection.getConnection();Statement s=c.createStatement()) {
 s.executeUpdate("CREATE DATABASE "+schema);
 try {
 System.setProperty("qpal.db.url","jdbc:mysql://localhost:3306/"+schema);
 try(Connection test=DbConnection.getConnection();Statement seed=test.createStatement()) {
 EmployeeStationDao.ensure(test);
 seed.executeUpdate("CREATE TABLE accounts(id INT PRIMARY KEY,name VARCHAR(50),email VARCHAR(100),role VARCHAR(20))");
 seed.executeUpdate("INSERT INTO accounts VALUES(1,'One','one@test','Employee'),(2,'Two','two@test','Employee'),(3,'Admin','admin@test','Admin')");
 seed.executeUpdate("INSERT INTO employee_queue_work(event,queue_entry_id,account_id,kind,station,passengers,amount,created_at) VALUES ('Payment',1,1,'Payment',1,2,100,'2026-10-09 23:59:59'),('Served',1,1,'Payment',1,2,0,'2026-10-09 23:59:59'),('Boarded',1,1,'Boarding',1,2,0,'2026-10-09 23:59:59'),('Payment',2,1,'Payment',1,1,50,'2026-10-10 00:00:00')");
 }
 var dao=new EmployeeSummaryDao();var admin=new Account(3,"Admin","admin@test","","Admin","Active");
 var rows=dao.dailyEmployees(admin,LocalDate.of(2026,10,9));
 if(rows.size()!=2 || rows.get(0).payments()!=1 || rows.get(0).collected().compareTo(new BigDecimal("100"))!=0 || rows.get(0).served()!=2 || rows.get(0).boarded()!=2 || rows.get(1).payments()!=0)throw new AssertionError("Daily totals or zero-work employee");
 if(dao.dailyEmployees(admin,LocalDate.of(2026,10,10)).get(0).collected().compareTo(new BigDecimal("50"))!=0)throw new AssertionError("Date boundary");
 try {dao.dailyEmployees(new Account(1,"One","one@test","","Employee","Active"),LocalDate.now());throw new AssertionError("Employee access");}catch(SQLException expected){}
 System.out.println("PASS: all employees, zero activity, separate served/boarded totals, Manila date boundaries and admin-only access");
 }finally{System.clearProperty("qpal.db.url");s.executeUpdate("DROP DATABASE "+schema);}
 }
 }
}
