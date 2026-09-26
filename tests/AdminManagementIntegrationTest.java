import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import qpal.dao.*;
import qpal.model.BookingData.*;
import static java.math.BigDecimal.TEN;

public class AdminManagementIntegrationTest {
    public static void run() throws Exception {
        PaymentDao payments = new PaymentDao();
        PaymentDao.Choice standalone = new PaymentDao.Choice(1,null,"Test trip",TEN);
        payments.save(null,standalone,"Passenger 1",TEN,"Cash","Pending");
        int id = BookingIntegrationTest.count("SELECT MAX(payment_id) FROM payments");
        PaymentDao.Payment original = payments.get(id);
        payments.save(original,standalone,"Passenger 1",new BigDecimal("15.50"),"Card","Paid");
        BookingIntegrationTest.check(payments.get(id).paidAt()!=null,"Paid timestamp populated");
        BookingIntegrationTest.check(new QueueDao().revenue().paidPassengers()==3,"Standalone paid payment counts one commuter");
        BookingIntegrationTest.rejects(() -> payments.save(original,standalone,"Stale",TEN,"Cash","Pending"));
        BookingIntegrationTest.rejects(() -> payments.save(null,standalone,"Passenger 1",new BigDecimal("-1"),"Cash","Paid"));
        payments.delete(payments.get(id));
        BookingIntegrationTest.check(BookingIntegrationTest.count("SELECT COUNT(*) FROM payments WHERE payment_id="+id)==0,"Standalone payment deleted");

        int linkedId = BookingIntegrationTest.count("SELECT MIN(payment_id) FROM payments");
        PaymentDao.Payment linked = payments.get(linkedId);
        PaymentDao.Choice booking = new PaymentDao.Choice(linked.tripId(),linked.bookingId(),"Booking",linked.amount());
        BookingIntegrationTest.rejects(() -> payments.save(linked,booking,"Passenger 1",TEN,"Cash","Paid"));
        payments.delete(linked);
        BookingIntegrationTest.check(BookingIntegrationTest.count("SELECT COUNT(*) FROM bookings WHERE booking_id="+linked.bookingId()+" AND status='Pending'")==1,"Deleting payment resets booking");
        BookingIntegrationTest.check(BookingIntegrationTest.count("SELECT COUNT(*) FROM queue_entries WHERE booking_id="+linked.bookingId()+" AND status='Waiting'")==1,"Deleting payment reopens completed queue");
        BookingIntegrationTest.check(payments.choices().stream().anyMatch(c -> linked.bookingId().equals(c.bookingId())),"Booking available for replacement payment");
        payments.save(null,booking,"Passenger 1",linked.amount(),"Cash","Paid");
        BookingIntegrationTest.rejects(() -> payments.save(null,booking,"Passenger 1",linked.amount(),"Cash","Paid"));
        BookingIntegrationTest.check(BookingIntegrationTest.count("SELECT COUNT(*) FROM bookings WHERE booking_id="+linked.bookingId()+" AND status='Confirmed'")==1,"Replacement payment syncs booking");

        int before = BookingIntegrationTest.count("SELECT COUNT(*) FROM payments");
        BookingIntegrationTest.sql("CREATE TRIGGER test_block_bus_delete BEFORE DELETE ON buses FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Rollback test'");
        BookingIntegrationTest.rejects(() -> new DeleteDao().delete(DeleteDao.Target.BUS,1));
        BookingIntegrationTest.check(BookingIntegrationTest.count("SELECT COUNT(*) FROM payments")==before,"Failed cascade restores all payments");
        BookingIntegrationTest.check(BookingIntegrationTest.count("SELECT COUNT(*) FROM seat_reservations")==5,"Failed cascade restores seats");
        BookingIntegrationTest.sql("DROP TRIGGER test_block_bus_delete");
        BookingIntegrationTest.check(new DeleteDao().delete(DeleteDao.Target.BUS,1),"Bus deleted with dependencies");
        for (String table : List.of("buses","trips","bookings","booking_passengers","seat_reservations","payments","queue_entries"))
            BookingIntegrationTest.check(BookingIntegrationTest.count("SELECT COUNT(*) FROM "+table)==0,"Cascade clears "+table);
        BookingIntegrationTest.check(BookingIntegrationTest.count("SELECT COUNT(*) FROM routes")==1,"Bus deletion keeps routes");
        BookingIntegrationTest.sql("INSERT INTO buses VALUES(2,'KEEP',20,20,'Available')");
        BookingIntegrationTest.sql("INSERT INTO routes(route_id,origin,destination,fare,status) VALUES(2,'PITX','Other',50,'Active')");
        BookingIntegrationTest.sql("INSERT INTO trips(trip_id,bus_id,route_id,departure_date,departure_time,available_seats,status) VALUES"
                + "(10,2,1,CURRENT_DATE+INTERVAL 1 DAY,'12:00:00',20,'Scheduled'),"
                + "(11,2,2,CURRENT_DATE+INTERVAL 1 DAY,'13:00:00',20,'Scheduled')");
        BookingDao bookings = new BookingDao();
        for(TripOption trip:bookings.availableTrips()) bookings.book(BookingIntegrationTest.reference(),trip,BookingIntegrationTest.passengers(1),"Cash");
        new DeleteDao().delete(DeleteDao.Target.ROUTE,1);
        BookingIntegrationTest.check(BookingIntegrationTest.count("SELECT COUNT(*) FROM buses")==1,"Route deletion keeps bus");
        BookingIntegrationTest.check(BookingIntegrationTest.count("SELECT COUNT(*) FROM trips")==1,"Route deletion keeps unrelated trip");
        BookingIntegrationTest.check(BookingIntegrationTest.count("SELECT COUNT(*) FROM bookings")==1,"Route deletion keeps unrelated booking");
        new DeleteDao().delete(DeleteDao.Target.TRIP,11);
        BookingIntegrationTest.check(BookingIntegrationTest.count("SELECT COUNT(*) FROM payments")==0,"Trip deletion clears payment");
        System.out.println("PASS: revenue CRUD, stale edits, booking synchronization, cascade rollback and isolated bus/route/trip deletion");
    }
}
