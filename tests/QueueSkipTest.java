package qpal.dao;

import java.sql.*;
import qpal.util.DbConnection;

public class QueueSkipTest {
    private static void check(Connection c, String sql, int expected) throws Exception {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            r.next();
            if (r.getInt(1) != expected) throw new AssertionError(sql);
        }
    }
    public static void main(String[] args) throws Exception {
        try (Connection c = DbConnection.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TEMPORARY TABLE payment_queue_skips(queue_entry_id INT PRIMARY KEY,skip_count INT) ENGINE=InnoDB");
            s.execute("CREATE TEMPORARY TABLE trips(trip_id INT PRIMARY KEY,available_seats INT) ENGINE=InnoDB");
            s.execute("CREATE TEMPORARY TABLE bookings(booking_id INT PRIMARY KEY,trip_id INT,status VARCHAR(20)) ENGINE=InnoDB");
            s.execute("CREATE TEMPORARY TABLE queue_entries(queue_entry_id INT PRIMARY KEY,booking_id INT,status VARCHAR(20)) ENGINE=InnoDB");
            s.execute("CREATE TEMPORARY TABLE booking_passengers(booking_passenger_id INT PRIMARY KEY,booking_id INT) ENGINE=InnoDB");
            s.execute("CREATE TEMPORARY TABLE seat_reservations(booking_passenger_id INT,trip_id INT,seat_number INT,status VARCHAR(20),active_seat_number INT GENERATED ALWAYS AS (CASE WHEN status='Active' THEN seat_number ELSE NULL END) STORED, UNIQUE KEY(trip_id,active_seat_number)) ENGINE=InnoDB");
            s.execute("CREATE TEMPORARY TABLE payments(booking_id INT,status VARCHAR(20)) ENGINE=InnoDB");
            s.execute("CREATE TEMPORARY TABLE queue_stations(kind VARCHAR(16),queue_entry_id INT) ENGINE=InnoDB");
            s.execute("INSERT INTO trips VALUES(1,8)");
            s.execute("INSERT INTO bookings VALUES(1,1,'Pending')");
            s.execute("INSERT INTO queue_entries VALUES(1,1,'Serving')");
            s.execute("INSERT INTO booking_passengers VALUES(1,1),(2,1)");
            s.execute("INSERT INTO seat_reservations(booking_passenger_id,trip_id,seat_number,status) VALUES(1,1,1,'Active'),(2,1,2,'Active')");
            s.execute("INSERT INTO payments VALUES(1,'Pending')");
            s.execute("INSERT INTO queue_stations VALUES('Payment',1)");
            c.setAutoCommit(false);
            for (int i=1; i<=3; i++) {
                QueueSkipDao.lockTrip(c,1);
                QueueSkipDao.skip(c,1,1);
                check(c,"SELECT available_seats FROM trips",i<3?8:10);
                check(c,"SELECT COUNT(*) FROM queue_entries WHERE status='No-show'",i<3?0:1);
                c.commit();
                if(i<3) s.execute("UPDATE queue_entries SET status='Serving'");
            }
            check(c,"SELECT skip_count FROM payment_queue_skips",3);
            check(c,"SELECT COUNT(*) FROM bookings WHERE status='No-show'",1);
            check(c,"SELECT COUNT(*) FROM payments WHERE status='Cancelled'",1);
            check(c,"SELECT COUNT(*) FROM queue_stations",0);
            // Releasing again cannot inflate capacity; another passenger can reserve the same seat.
            QueueSkipDao.skip(c,1,1);
            check(c,"SELECT available_seats FROM trips",10);
            s.execute("INSERT INTO seat_reservations(booking_passenger_id,trip_id,seat_number,status) VALUES(3,1,1,'Active')");
            check(c,"SELECT COUNT(*) FROM seat_reservations WHERE status='Active'",1);
            c.rollback();
            System.out.println("PASS: third skip closes queue, frees seats once, clears station, and permits seat reuse.");
        }
    }
}
