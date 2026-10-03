package qpal.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import qpal.model.EmployeeStation;
import qpal.model.BookingData.QueueRow;
import qpal.util.DbConnection;
import static qpal.dao.BookingDao.*;

public final class EmployeeDashboardDao {
    public record Summary(int commuters, BigDecimal collected, int pending, int sharedWaiting,
            QueueRow current, List<DashboardDao.BoardingTrip> activeBoarding, String stationTrip) {}

    public List<QueueRow> stationQueues(EmployeeStation station) throws SQLException {
        QueueDao dao=new QueueDao();
        if (!station.boarding()) {
            Integer current=dao.stations("Payment").get(station.number());
            return dao.today().stream().filter(r -> "Waiting".equals(r.status()) || "Skipped".equals(r.status())
                    || ("Serving".equals(r.status()) && Objects.equals(current,r.id()))).toList();
        }
        List<QueueRow> rows=dao.boarding();
        Set<Integer> ids=new HashSet<>();
        try (Connection c=DbConnection.getConnection(); PreparedStatement p=statement(c,
                "SELECT q.queue_entry_id FROM queue_entries q JOIN bookings b ON b.booking_id=q.booking_id "
                + "JOIN boarding_gates g ON g.trip_id=b.trip_id WHERE g.gate=?",station.number()); ResultSet r=p.executeQuery()) {
            while (r.next()) ids.add(r.getInt(1));
        }
        return rows.stream().filter(r -> ids.contains(r.id())).toList();
    }

    public Summary load(EmployeeStationDao.Session session) throws SQLException {
        var station=session.station();
        var rows=stationQueues(station);
        var assigned=new QueueDao().stations(station.kind()).get(station.number());
        QueueRow current=rows.stream().filter(r -> Objects.equals(assigned,r.id())).findFirst().orElse(null);
        int served=0; BigDecimal collected=BigDecimal.ZERO;
        var live=new DashboardDao().loadSummary();
        String stationName=(station.boarding() ? "Boarding Gate " : "Payment Counter ")+station.number();
        String stationTrip=live.stations.stream().filter(s -> s.name().equals(stationName)).map(DashboardDao.Station::trip)
                .filter(Objects::nonNull).findFirst().orElse(null);
        try (Connection c=DbConnection.getConnection()) {
            try (PreparedStatement p=statement(c,"SELECT COALESCE(SUM(CASE WHEN event IN ('Served','Boarded') THEN passengers ELSE 0 END),0),"
                    + "COALESCE(SUM(CASE WHEN event='Payment' THEN amount ELSE 0 END),0) FROM employee_queue_work "
                    + "WHERE account_id=? AND created_at>=CURRENT_DATE AND created_at<CURRENT_DATE+INTERVAL 1 DAY",session.accountId()); ResultSet r=p.executeQuery()) {
                r.next(); served=r.getInt(1); collected=r.getBigDecimal(2);
            }
        }
        int shared=(int)rows.stream().filter(r -> "Waiting".equals(r.status()) || "Skipped".equals(r.status())).count();
        return new Summary(served,collected,station.boarding()?rows.size():(current==null?0:1),shared,current,live.boardingTrips,stationTrip);
    }
}

