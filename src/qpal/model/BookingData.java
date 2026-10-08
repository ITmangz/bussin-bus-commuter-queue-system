package qpal.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class BookingData {
    private BookingData() {}
    public record TripOption(int id, String bus, String origin, String destination,
            LocalDate date, LocalTime time, BigDecimal fare, int capacity, int available) {
        public String route() { return origin + " - " + destination; }
        public String schedule() { return date + " | " + time; }
    }
    public record Passenger(String name, String type, int seat) {}
    public record Receipt(int bookingId, String reference, LocalDate queueDate,
            int queueNumber, String bus, String route, String schedule, String seats,
            BigDecimal total, String method, String paymentStatus) {
        public String text() { return text("P"); }
        public String text(String prefix) {
            return "BUSSIN TICKET\n\n" + detailsText(prefix)
                    + ("Paid".equals(paymentStatus) ? "" : "\nPlease pay at the counter.");
        }
        public String detailsText(String prefix) {
            return "Queue: " + queueDate + " / " + String.format(java.util.Locale.ROOT,"%s%03d",prefix,queueNumber)
                    + "\nBooking: " + reference + "\nBus: " + bus + "\nRoute: " + route
                    + "\nDeparture: " + schedule + "\nSeats: " + seats
                    + "\nTotal: PHP " + total + "\nPayment: " + method + " - " + paymentStatus;
        }
    }
    public record QueueRow(int id, int bookingId, int number, String route, String bus,
            String schedule, String passenger, String payment, String status, int passengers) {}
    public record RevenueData(List<Object[]> rows, int paidPassengers, int pendingPayments,
            BigDecimal todayRevenue, BigDecimal totalRevenue) {}
}
