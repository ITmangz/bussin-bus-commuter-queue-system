import java.sql.*;
import qpal.dao.QueueDao;
import qpal.util.DbConnection;

/** Verifies the revenue query against the current database without writing data. */
public class RevenueReadOnlyTest {
    public static void main(String[] args) throws Exception {
        var data=new QueueDao().revenue();
        long paid=data.rows().stream().filter(row -> "Paid".equals(row[6])).count();
        if(paid!=data.paidPassengers()) throw new AssertionError("Paid rows differ from summary");
        try(Connection c=DbConnection.getConnection(); Statement s=c.createStatement();
                ResultSet r=s.executeQuery("SELECT COALESCE(SUM(amount),0) FROM payments WHERE status='Paid' AND DATE(paid_at)=CURRENT_DATE")) {
            r.next();
            if(r.getBigDecimal(1).compareTo(data.todayRevenue())!=0) throw new AssertionError("Revenue duplicated");
        }
        try(Connection c=DbConnection.getConnection(); Statement s=c.createStatement();
                ResultSet r=s.executeQuery("SELECT COALESCE(SUM(amount),0) FROM payments WHERE status='Paid'")) {
            r.next();
            if(r.getBigDecimal(1).compareTo(data.totalRevenue())!=0) throw new AssertionError("All-time revenue differs from paid payments");
        }
        System.out.println("PASS: "+paid+" paid commuter rows match summary; daily and all-time revenue counted once per payment");
    }
}
