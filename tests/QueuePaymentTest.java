import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import qpal.dao.*;
import qpal.model.BookingData.*;
import qpal.util.DbConnection;
import qpal.view.Admin.QueuePaymentDialog;

public class QueuePaymentTest {
    static void check(boolean value,String message) { if (!value) throw new AssertionError(message); }
    interface Action { void run() throws Exception; }
    static void rejects(Action action) throws Exception {
        try { action.run(); throw new AssertionError("Expected rejection"); }
        catch (SQLException | IllegalArgumentException expected) {}
    }
    public static void main(String[] args) throws Exception {
        BigDecimal total=new BigDecimal("500.00");
        rejects(() -> QueuePaymentDao.validateReceived("",total));
        rejects(() -> QueuePaymentDao.validateReceived("NaN",total));
        rejects(() -> QueuePaymentDao.validateReceived("499.99",total));
        rejects(() -> QueuePaymentDao.validateReceived("-500",total));
        rejects(() -> QueuePaymentDao.validateReceived("500.001",total));
        check(QueuePaymentDao.validateReceived("1000",total).subtract(total).equals(total),"Correct change");
        String seats=String.join(", ",java.util.stream.IntStream.rangeClosed(1,10).mapToObj(i -> "A"+i).toList());
        Receipt sample=new Receipt(1,"TEST",LocalDate.now(),1,"BUS","A - B","Tomorrow",seats,total,"Cash","Paid");
        var tickets=QueuePaymentDialog.boardingTickets(sample);
        check(tickets.size()==10,"Ten passengers produce ten individual pages");
        check(new HashSet<>(tickets).size()==10,"Every ticket identifies its own seat");
        if (args.length==0) { System.out.println("Payment validation and ten-ticket checks passed."); return; }
        String schema="qpal_integration_test_"+UUID.randomUUID().toString().replace("-","");
        try (Connection setup=DbConnection.getConnection(); Statement s=setup.createStatement()) {
            s.executeUpdate("CREATE DATABASE "+schema);
            try {
                for (String table:List.of("buses","routes","trips","bookings","booking_passengers","seat_reservations","payments","queue_entries","queue_daily_counters"))
                    s.executeUpdate("CREATE TABLE "+schema+"."+table+" LIKE qpal."+table);
                System.setProperty("qpal.db.url","jdbc:mysql://localhost:3306/"+schema);
                try (Connection c=DbConnection.getConnection(); Statement seed=c.createStatement()) {
                    seed.executeUpdate("INSERT INTO buses(bus_id,bus_number,seat_capacity,available_seats,bus_status) VALUES(1,'TEST',20,20,'Available')");
                    seed.executeUpdate("INSERT INTO routes(route_id,origin,destination,fare,status) VALUES(1,'A','B',50,'Active')");
                    seed.executeUpdate("INSERT INTO trips(trip_id,bus_id,route_id,departure_date,departure_time,available_seats,status) VALUES(1,1,1,CURRENT_DATE+INTERVAL 1 DAY,'12:00:00',20,'Scheduled')");
                }
                BookingDao bookings=new BookingDao(); QueueDao queues=new QueueDao(); QueuePaymentDao payments=new QueuePaymentDao();
                var passengers=java.util.stream.IntStream.rangeClosed(1,10).mapToObj(i -> new Passenger("Passenger "+i,"Regular",i)).toList();
                var booking=bookings.book("PAYMENT-TEST",bookings.availableTrips().get(0),passengers,"Cash");
                int queue=queues.today().get(0).id();
                rejects(() -> payments.pay(queue,1,total));
                queues.act(0,"Call Next Queue",1);
                rejects(() -> payments.pay(queue,2,total));
                rejects(() -> payments.pay(queue,1,new BigDecimal("499")));
                check(bookings.receipt(booking.bookingId()).paymentStatus().equals("Pending"),"Underpayment rolls back");
                payments.pay(queue,1,new BigDecimal("1000"));
                rejects(() -> payments.pay(queue,1,new BigDecimal("1000")));
                check(payments.progress(queue).received().equals(new BigDecimal("1000.00")),"Tender survives reload");
                rejects(() -> queues.act(queue,"Complete",1));
                rejects(() -> payments.printed(queue,true));
                payments.printed(queue,false);
                rejects(() -> queues.act(queue,"Complete",1));
                payments.printed(queue,true);
                check(new QueuePaymentDao().progress(queue).ticketsPrinted(),"Print progress survives reopening");
                queues.act(queue,"Complete",1);
                check(queues.today().get(0).status().equals("Completed"),"Completion after all documents");
                System.out.println("Payment transaction and completion integration checks passed.");
            } finally { System.clearProperty("qpal.db.url"); s.executeUpdate("DROP DATABASE "+schema); }
        }
    }
}
