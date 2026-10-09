import java.sql.*;
import java.util.*;
import java.math.BigDecimal;
import qpal.dao.*;
import qpal.model.BookingData.*;
import qpal.util.DbConnection;

public class QueueManagementTest {
    static void check(boolean ok,String message) { if (!ok) throw new AssertionError(message); }
    static void sql(String query) throws Exception {
        try(Connection c=DbConnection.getConnection();Statement s=c.createStatement()){s.executeUpdate(query);}
    }
    static int count(String query) throws Exception {
        try(Connection c=DbConnection.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery(query)){r.next();return r.getInt(1);}
    }
    interface Action { void run() throws Exception; }
    static void rejects(Action a) throws Exception { try {a.run();throw new AssertionError("Expected rejection");}catch(SQLException expected){} }
    public static void main(String[] args) throws Exception {
        String schema="qpal_integration_test_"+UUID.randomUUID().toString().replace("-","");
        try(Connection setup=DbConnection.getConnection();Statement s=setup.createStatement()) {
            s.executeUpdate("CREATE DATABASE "+schema);
            try {
                for(String table:List.of("buses","routes","trips","bookings","booking_passengers","seat_reservations","payments","queue_entries","queue_daily_counters"))
                    s.executeUpdate("CREATE TABLE "+schema+"."+table+" LIKE qpal."+table);
                System.setProperty("qpal.db.url","jdbc:mysql://localhost:3306/"+schema);
                sql("INSERT INTO buses(bus_id,bus_number,seat_capacity,available_seats,bus_status) VALUES(1,'TEST',20,20,'Available')");
                sql("INSERT INTO routes(route_id,origin,destination,fare,status) VALUES(1,'Test A','Test B',50,'Active')");
                sql("INSERT INTO trips(trip_id,bus_id,route_id,departure_date,departure_time,available_seats,status) VALUES(1,1,1,CURRENT_DATE+INTERVAL 1 DAY,'12:00:00',20,'Scheduled')");
                BookingDao bookings=new BookingDao(); QueueDao queue=new QueueDao(); QueueBookingDao edit=new QueueBookingDao();
                TripOption trip=bookings.availableTrips().get(0);
                Receipt first=bookings.book("TEST-ONE",trip,List.of(new Passenger("First","Regular",1)),"Cash");
                Receipt second=bookings.book("TEST-TWO",trip,List.of(new Passenger("Second","Regular",2)),"Cash");
                var rows=queue.today(); int one=rows.get(0).id(),two=rows.get(1).id();
                rejects(()->queue.act(two,"Recall"));
                queue.act(0,"Call Next Queue");
                check(queue.today().get(0).status().equals("Serving"),"FIFO call");
                                rejects(()->queue.act(0,"Call Next Queue",0));
                queue.act(0,"Call Next Queue",2);
                check(queue.stations("Payment").get(1)==one && queue.stations("Payment").get(2)==two,"Independent counter assignments");
                rejects(()->queue.act(one,"Skip Queue",2));
                rejects(()->queue.act(0,"Call Next Queue",2));
                queue.act(two,"Undo Call",2);
                queue.act(one,"Undo Call");
                check(queue.today().get(0).status().equals("Waiting"),"Undo returns to Waiting");
                queue.act(0,"Call Next Queue");
                queue.act(one,"Skip Queue");
                queue.act(0,"Call Next Queue");
                queue.act(two,"Undo Call");
                queue.act(one,"Recall");
                check(queue.today().get(0).status().equals("Serving"),"Recall earlier skipped row");
                var original=edit.passengers(first.bookingId());
                rejects(()->edit.edit(first.bookingId(),original,List.of(new QueueBookingDao.Person(original.get(0).id(),"Updated","Student"))));
                edit.edit(first.bookingId(),original,List.of(new QueueBookingDao.Person(original.get(0).id(),"Updated","Regular")));
                check(edit.passengers(first.bookingId()).get(0).name().equals("Updated"),"Passenger edit saved");
                rejects(()->edit.edit(first.bookingId(),original,original));
                sql("CREATE TRIGGER reject_queue_cancel BEFORE UPDATE ON queue_entries FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Rollback test'");
                rejects(()->edit.cancel(first.bookingId()));
                check(count("SELECT available_seats FROM trips WHERE trip_id=1")==18,"Failure rolls availability back");
                check(bookings.occupiedSeats(1).contains(1),"Failure preserves seat");
                sql("DROP TRIGGER reject_queue_cancel");
                edit.cancel(first.bookingId());
                check(count("SELECT available_seats FROM trips WHERE trip_id=1")==19,"Cancel frees exactly one seat");
                check(!bookings.occupiedSeats(1).contains(1),"Seat reusable");
                check(queue.today().get(0).status().equals("Cancelled"),"Cancellation retained in history");
                rejects(()->edit.cancel(first.bookingId()));
                check(count("SELECT available_seats FROM trips WHERE trip_id=1")==19,"Repeat cancel cannot overcount");
                queue.act(two,"Mark as Paid");
                edit.cancel(second.bookingId());
                check(count("SELECT COUNT(*) FROM payments WHERE status='Paid'")==1,"Paid financial record preserved");
                check(count("SELECT available_seats FROM trips WHERE trip_id=1")==20,"All seats returned");
                Receipt mixed=bookings.book("TEST-DISCOUNTS",trip,List.of(new Passenger("Regular","Regular",1),new Passenger("Student","Student",2),new Passenger("PWD","PWD",3),new Passenger("Senior","Senior",4)),"GCash");
                check(mixed.total().compareTo(new BigDecimal("170"))==0,"Mixed discount total saved");
                check(mixed.fareBreakdown().contains("Fare subtotal: PHP 200") && mixed.fareBreakdown().contains("Total discount: PHP 30"),"Receipt breakdown saved");
                sql("UPDATE routes SET fare=100 WHERE route_id=1");
                check(bookings.receipt(mixed.bookingId()).fareBreakdown().equals(mixed.fareBreakdown()),"Reprint preserves original fare after route change");
                check(count("SELECT COUNT(*) FROM booking_passengers WHERE booking_id="+mixed.bookingId()+" AND fare=40")==3,"Individual discounts saved");
                check(count("SELECT COUNT(*) FROM payments WHERE booking_id="+mixed.bookingId()+" AND amount=170 AND status='Pending'")==1,"Cashless total pending verification");
                check(bookings.book("TEST-DISCOUNTS",trip,List.of(new Passenger("Regular","Regular",1)),"GCash").queueNumber()==mixed.queueNumber(),"Retry retains queue identity");
                System.out.println("Queue management and mixed discount integration checks passed.");
            } finally {
                System.clearProperty("qpal.db.url");
                s.executeUpdate("DROP DATABASE "+schema);
            }
        }
    }
}